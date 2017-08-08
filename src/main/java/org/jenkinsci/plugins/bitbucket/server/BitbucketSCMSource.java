package org.jenkinsci.plugins.bitbucket.server;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.browser.Stash;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.Branch;
import org.jenkinsci.plugins.bitbucket.server.api.model.Link;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketPagingClient;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientService;
import org.jenkinsci.plugins.bitbucket.server.filter.BranchFilter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.allOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;

/**
 * @author Robin Müller
 */
public class BitbucketSCMSource extends SCMSource {

    private final BitbucketClientConfiguration clientConfiguration;
    private final String project;
    private final String repository;
    private boolean autoRegisterHook;
    private String checkoutCredentialsId;
    private String includes = "*";
    private String excludes;

    @DataBoundConstructor
    public BitbucketSCMSource(String id, String bitbucketServerUrl, String scanCredentialsId, String project, String repository) {
        this(id, aBitbucketClientConfiguration().baseUrl(bitbucketServerUrl).credentialsId(scanCredentialsId).build(), project, repository);
    }

    BitbucketSCMSource(String id, BitbucketClientConfiguration clientConfiguration, String project, String repository) {
        super(id);
        this.clientConfiguration = clientConfiguration;
        this.project = project;
        this.repository = repository;
    }

    public String getBitbucketServerUrl() {
        return clientConfiguration.getBaseUrl();
    }

    public String getScanCredentialsId() {
        return clientConfiguration.getCredentialsId();
    }

    public String getProject() {
        return project;
    }

    public String getRepository() {
        return repository;
    }

    public boolean isAutoRegisterHook() {
        return autoRegisterHook;
    }

    @DataBoundSetter
    public void setAutoRegisterHook(boolean autoRegisterHook) {
        this.autoRegisterHook = autoRegisterHook;
    }

    public String getCheckoutCredentialsId() {
        return checkoutCredentialsId;
    }

    @DataBoundSetter
    public void setCheckoutCredentialsId(String checkoutCredentialsId) {
        this.checkoutCredentialsId = checkoutCredentialsId;
    }

    public String getIncludes() {
        return includes;
    }

    @DataBoundSetter
    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    @DataBoundSetter
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public BitbucketServerAPI getClient() {
        return BitbucketServerClientService.instance().getClient(clientConfiguration, getOwner());
    }

    @Override
    protected void retrieve(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer, @CheckForNull SCMHeadEvent<?> event, @NonNull TaskListener listener) throws IOException, InterruptedException {
        retrieveBranches(criteria, observer, listener);
    }

    private void retrieveBranches(SCMSourceCriteria criteria, SCMHeadObserver observer, TaskListener listener) throws InterruptedException, IOException {
        BitbucketPagingClient client = new BitbucketPagingClient(getClient());
        listener.getLogger().printf("Looking up %s/%s for branches%n", project, repository);
        Set<SCMHead> includedHeads = observer.getIncludes();
        BranchFilter filter = new BranchFilter(includes, excludes);
        try {
            for (Branch branch : client.getBranches(project, repository)) {
                checkInterrupt();
                SCMHead head = new BranchSCMHead(branch.getDisplayId());
                if (includedHeads != null && !includedHeads.contains(head)) {
                    continue;
                }
                if (filter.isBranchAllowed(branch.getDisplayId())) {
                    AbstractGitSCMSource.SCMRevisionImpl revision = new AbstractGitSCMSource.SCMRevisionImpl(head, branch.getLatestCommit());
                    listener.getLogger().printf("Checking branch %s form %s/%s%n", branch.getDisplayId(), project, repository);
                    if (criteria != null) {
                        if (criteria.isHead(createProbe(head, revision), listener)) {
                            listener.getLogger().println("Met criteria");
                        } else {
                            listener.getLogger().println("Does not meet criteria");
                            continue;
                        }
                    }
                    observer.observe(head, revision);
                }
                if (!observer.isObserving()) {
                    return;
                }
            }
        } catch (NotFoundException e) {
            listener.getLogger().printf("Project (%s) or repository (%s) doesn't exist anymore", project, repository);
        }
    }

    @NonNull
    @Override
    public SCM build(@NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        Map<String, List<Link>> links = getLinks();
        return new GitSCM(Collections.singletonList(new UserRemoteConfig(retrieveCloneUrl(links), null, null, retrieveCheckoutCredentialsId())),
                          Collections.singletonList(new BranchSpec(head.getName())),
                          false,
                          Collections.<SubmoduleConfig>emptyList(),
                          new Stash(getBitbucketRepositoryBrowserUrl(links)),
                          null,
                          null);
    }

    private String retrieveCheckoutCredentialsId() {
        if (StringUtils.equals(checkoutCredentialsId, DescriptorImpl.SAME)) {
            return clientConfiguration.getCredentialsId();
        } else {
            return checkoutCredentialsId;
        }
    }

    @NonNull
    @Override
    protected SCMProbe createProbe(@NonNull SCMHead head, @CheckForNull SCMRevision revision) throws IOException {
        return new BitbucketSCMProbe(head, revision, clientConfiguration, getOwner(), project, repository);
    }

    private Map<String, List<Link>> getLinks() {
        return getClient().getRepository(project, repository).getLinks();
    }

    private String getBitbucketRepositoryBrowserUrl(Map<String, List<Link>> links) {
        return links.get("self").get(0).getHref().replaceFirst("/browse$", "");
    }

    String retrieveCloneUrl(Map<String, List<Link>> links) {
        String httpUrl = null;
        for (Link clone : links.get("clone")) {
            if (clone.getName().equals("ssh") && isSshCheckout(retrieveCheckoutCredentialsId(), clone.getHref())) {
                return clone.getHref();
            } else if (clone.getName().equals("http")) {
                httpUrl = clone.getHref();
            }
        }
        return httpUrl;
    }

    private boolean isSshCheckout(String credentialsId, String sshUrl) {
        return credentialsId != null && findSshCredentials(credentialsId, sshUrl) != null;
    }

    private SSHUserPrivateKey findSshCredentials(String credentialsId, String url) {
        SCMSourceOwner context = getOwner();
        return firstOrNull(lookupCredentials(SSHUserPrivateKey.class,
                                             context,
                                             context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
                                             URIRequirementBuilder.fromUri(url).build()),
                           allOf(withId(credentialsId), anyOf(instanceOf(SSHUserPrivateKey.class))));
    }

    @Extension
    public static class DescriptorImpl extends SCMSourceDescriptor {

        public static final String SAME = FormUtils.SAME;

        @Override
        public String getDisplayName() {
            return Messages.BitbucketSCMSource_DisplayName();
        }

        public FormValidation doCheckScanCredentialsId(@QueryParameter String value) {
            return FormUtils.checkScanCredentialsId(value);
        }

        public FormValidation doCheckBitbucketServerUrl(@QueryParameter String value) {
            return FormUtils.checkBitbucketServerUrl(value);
        }

        public FormValidation doCheckProject(@QueryParameter String value) {
            return FormUtils.checkRequiredField(value, Messages.Error_ProjectRequired());
        }

        public FormValidation doCheckRepository(@QueryParameter String value) {
            return FormUtils.checkRequiredField(value, Messages.Error_RepositoryRequired());
        }

        public FormValidation doCheckIncludes(@QueryParameter String value) {
            return FormUtils.checkRequiredField(value, Messages.Error_IncludesRequired());
        }


        public ListBoxModel doFillScanCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String bitbucketServerUrl) {
            return FormUtils.fillScanCredentials(bitbucketServerUrl, context);
        }

        public ListBoxModel doFillCheckoutCredentialsIdItems(@AncestorInPath SCMSourceOwner context, @QueryParameter String bitbucketServerUrl) {
            return FormUtils.fillCheckoutCredentials(bitbucketServerUrl, context);
        }

        @NonNull
        @Override
        protected SCMHeadCategory[] createCategories() {
            return new SCMHeadCategory[]{
                    new UncategorizedSCMHeadCategory(Messages._BitbucketSCMSource_UncategorizedSCMHeadCategory_DisplayName()),
                    new ChangeRequestSCMHeadCategory(Messages._BitbucketSCMSource_ChangeRequestSCMHeadCategory_DisplayName())
            };
        }
    }
}
