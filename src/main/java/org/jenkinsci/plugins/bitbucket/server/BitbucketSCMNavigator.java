package org.jenkinsci.plugins.bitbucket.server;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSourceCategory;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.impl.UncategorizedSCMSourceCategory;
import org.jenkinsci.plugins.bitbucket.server.api.model.Repository;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketPagingClient;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientService;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;

/**
 * @author Robin Müller
 */
public class BitbucketSCMNavigator extends SCMNavigator {

    private final BitbucketClientConfiguration clientConfiguration;
    private final String project;
    private String pattern = ".*";
    private String checkoutCredentialsId;
    private String includes = "*";
    private String excludes;

    @DataBoundConstructor
    public BitbucketSCMNavigator(String bitbucketServerUrl, String scanCredentialsId, String project) {
        this(aBitbucketClientConfiguration().baseUrl(bitbucketServerUrl).credentialsId(scanCredentialsId).build(), project);
    }

    BitbucketSCMNavigator(BitbucketClientConfiguration clientConfiguration, String project) {
        this.clientConfiguration = clientConfiguration;
        this.project = project;
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

    public String getPattern() {
        return pattern;
    }

    @DataBoundSetter
    public void setPattern(String pattern) {
        this.pattern = pattern;
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

    @NonNull
    @Override
    protected String id() {
        return clientConfiguration.getBaseUrl() + "::" + project;
    }

    @Override
    public void visitSources(@Nonnull SCMSourceObserver observer) throws IOException, InterruptedException {
        TaskListener listener = observer.getListener();

        BitbucketPagingClient client = BitbucketServerClientService.instance().getPagingClient(clientConfiguration, observer.getContext());

        listener.getLogger().printf("Looking up repositories of project %s%n", project);
        for (Repository repository : client.getRepositories(project)) {
            String repositorySlug = repository.getSlug();
            if (!repositorySlug.matches(pattern)) {
                listener.getLogger().printf("Ignoring %s%n", repositorySlug);
                continue;
            }
            listener.getLogger().printf("Proposing %s%n", repositorySlug);
            checkInterrupt();
            SCMSourceObserver.ProjectObserver projectObserver = observer.observe(repository.getName());
            BitbucketSCMSource scmSource =
                    new BitbucketSCMSource(getId() + "::" + repositorySlug, clientConfiguration, project, repositorySlug);
            scmSource.setCheckoutCredentialsId(checkoutCredentialsId);
            scmSource.setIncludes(includes);
            scmSource.setExcludes(excludes);
            projectObserver.addSource(scmSource);
            projectObserver.complete();
        }
    }

    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        public static final String SAME = FormUtils.SAME;

        @Override
        public String getDisplayName() {
            return Messages.BitbucketSCMNavigator_DisplayName();
        }

        @NonNull
        @Override
        public String getDescription() {
            return Messages.BitbucketSCMNavigator_Description();
        }

        @Override
        public SCMNavigator newInstance(String name) {
            return new BitbucketSCMNavigator(null, null, name);
        }

        public FormValidation doCheckScanCredentialsId(@QueryParameter String value) {
            return FormUtils.checkScanCredentialsId(value);
        }

        public FormValidation doCheckBitbucketServerUrl(@QueryParameter String value) {
            return FormUtils.checkBitbucketServerUrl(value);
        }

        public FormValidation doCheckPattern(@QueryParameter String value) {
            String pattern = Util.fixEmpty(value);
            if (pattern == null) {
                return FormValidation.error(Messages.Error_PatternRequired());
            }
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                return FormValidation.error(Messages.Error_InvalidPattern(e.getMessage()));
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckProject(@QueryParameter String value) {
            return FormUtils.checkRequiredField(value, Messages.Error_ProjectRequired());
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
        protected SCMSourceCategory[] createCategories() {
            return new SCMSourceCategory[]{
                    new UncategorizedSCMSourceCategory(Messages._BitbucketSCMNavigator_UncategorizedSCMSourceCategory_DisplayName())
            };
        }
    }
}
