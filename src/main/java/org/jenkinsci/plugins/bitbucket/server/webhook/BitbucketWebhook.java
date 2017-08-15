package org.jenkinsci.plugins.bitbucket.server.webhook;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMEvent;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.bitbucket.server.BranchSCMHead;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookEvent;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Robin Müller
 */
@Extension
public class BitbucketWebhook extends CrumbExclusion implements UnprotectedRootAction {

    public static final String PATH = "bitbucket-server-webhook";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return PATH;
    }

    public void getDynamic(String addOnKey, StaplerRequest request) throws IOException {
        String origin = SCMHeadEvent.originOf(request);
        boolean addOnNotFound = true;
        for (BitbucketWebHookProvider bitbucketWebHookProvider : Jenkins.getActiveInstance().getExtensionList(BitbucketWebHookProvider.class)) {
            if (bitbucketWebHookProvider.getAddonKey().equals(addOnKey)) {
                addOnNotFound = false;
                for (BitbucketWebHookEvent event : bitbucketWebHookProvider.processWebHook(request)) {
                    SCMHeadEvent.fireNow(new BitbucketSCMHeadEvent(origin, event));
                }
            }
        }
        if (addOnNotFound) {
            throw HttpResponses.notFound();
        }
        throw HttpResponses.ok();
    }

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith('/' + PATH + '/')) {
            chain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private static class BitbucketSCMHeadEvent extends SCMHeadEvent<BitbucketWebHookEvent> {

        BitbucketSCMHeadEvent(String origin, BitbucketWebHookEvent event) {
            super(retrieveType(event.getType()), event, origin);
        }

        private static SCMEvent.Type retrieveType(BitbucketWebHookEvent.Type type) {
            switch (type) {
                case CREATE:
                    return Type.CREATED;
                case UPDATE:
                    return Type.UPDATED;
                case DELETE:
                    return Type.REMOVED;
                default:
                    throw new IllegalStateException("Unknown event type: " + type);
            }
        }

        @Override
        public boolean isMatch(@NonNull SCMNavigator scmNavigator) {
            return false;
        }

        @NonNull
        @Override
        public String getSourceName() {
            BitbucketWebHookEvent payload = getPayload();
            return payload.getProject() + "/" + payload.getRepository();
        }

        @NonNull
        @Override
        public Map<SCMHead, SCMRevision> heads(@NonNull SCMSource scmSource) {
            BitbucketWebHookEvent payload = getPayload();
            String commitId = payload.getCommitId();
            SCMHead head = new BranchSCMHead(payload.getBranch());
            return Collections.<SCMHead, SCMRevision>singletonMap(head, commitId != null ? new AbstractGitSCMSource.SCMRevisionImpl(head, commitId) : null);
        }

        @Override
        public boolean isMatch(@NonNull SCM scm) {
            if (getType() != Type.CREATED && scm instanceof GitSCM) {
                for (UserRemoteConfig userRemoteConfig : ((GitSCM) scm).getUserRemoteConfigs()) {
                    try {
                        String refspec = userRemoteConfig.getRefspec();
                        return isMatch(new URIish(userRemoteConfig.getUrl())) && refspec != null && refspec.endsWith(getPayload().getBranch());
                    } catch (URISyntaxException e) {
                        // nothing to do
                    }
                }
            }
            return false;
        }

        private boolean isMatch(URIish urIish) {
            BitbucketWebHookEvent payload = getPayload();
            String project = payload.getProject();
            String repository = payload.getRepository();
            String httpUrlPath = "/scm/" + project + "/" + repository + ".git";
            String sshUrlPath = "/" + project.toLowerCase() + "/" + repository + ".git";
            return (StringUtils.equals(urIish.getScheme(), "https") && StringUtils.equals(urIish.getPath(), httpUrlPath))
                   || (StringUtils.equals(urIish.getScheme(), "ssh") && StringUtils.equals(urIish.getPath(), sshUrlPath));
        }
    }
}
