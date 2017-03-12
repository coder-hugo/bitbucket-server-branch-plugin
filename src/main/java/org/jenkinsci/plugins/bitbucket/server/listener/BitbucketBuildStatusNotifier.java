package org.jenkinsci.plugins.bitbucket.server.listener;

import hudson.model.ItemGroup;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.Revision;
import hudson.plugins.git.util.BuildData;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.BitbucketSCMSource;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.BuildStatus;
import org.jenkinsci.plugins.bitbucket.server.api.model.builder.BuildStatusBuilder;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientService;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.BuildStatusBuilder.aBuildStatus;
import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;

/**
 * @author Robin Müller
 */
final class BitbucketBuildStatusNotifier {

    private BitbucketBuildStatusNotifier() {
    }

    static void updateBuildStatus(Run<?, ?> build, TaskListener listener) {
        BitbucketSCMSource scmSource = retrieveBitbucketSCMSource(build);
        if (scmSource != null) {
            if (scmSource.getScanCredentialsId() == null) {
                return;
            }
            try {
                String commitId = retrieveBuildRevision(build);
                try {
                    BitbucketServerAPI client = getClient(scmSource);
                    client.updateBuildStatus(commitId, retrieveBuildStatus(build));
                    listener.getLogger().println("Updated Bitbucket Server commit status.");
                } catch (WebApplicationException | ProcessingException e) {
                    listener.getLogger().printf("Failed to update Bitbucket Server commit status for commit '%s': %s%n", commitId, e.getMessage());
                }
            } catch (IllegalStateException e) {
                listener.getLogger().printf("Failed to update Bitbucket Server commit status: %s%n", e.getMessage());
            }
        }
    }

    private static BitbucketServerAPI getClient(BitbucketSCMSource scmSource) {
        return BitbucketServerClientService.instance().getClient(aBitbucketClientConfiguration()
                                                                         .baseUrl(scmSource.getBitbucketServerUrl())
                                                                         .credentialsId(scmSource.getScanCredentialsId())
                                                                         .build(),
                                                                 scmSource.getOwner());
    }

    private static BitbucketSCMSource retrieveBitbucketSCMSource(Run<?, ?> build) {
        ItemGroup<?> itemGroup = build.getParent().getParent();
        if (itemGroup instanceof SCMSourceOwner) {
            for (SCMSource scmSource : ((SCMSourceOwner) itemGroup).getSCMSources()) {
                if (scmSource instanceof BitbucketSCMSource) {
                    return (BitbucketSCMSource) scmSource;
                }
            }
        }
        return null;
    }

    private static BuildStatus retrieveBuildStatus(Run<?, ?> build) {
        BuildStatusBuilder builder = aBuildStatus()
                .key(build.getParent().getName())
                .name(build.getDisplayName())
                .url(retrieveBuildUrl(build));

        Result result = build.getResult();
        if (Result.SUCCESS.equals(result)) {
            builder.state(BuildStatus.State.SUCCESSFUL).description("This commit looks good");
        } else if (Result.UNSTABLE.equals(result)) {
            builder.state(BuildStatus.State.FAILED).description("This commit has test failures");
        } else if (Result.FAILURE.equals(result)) {
            builder.state(BuildStatus.State.FAILED).description("There was a failure building this commit");
        } else if (result != null) { // ABORTED etc.
            builder.state(BuildStatus.State.FAILED).description("Something is wrong with the build of this commit");
        } else {
            builder.state(BuildStatus.State.INPROGRESS).description("The tests have started...");
        }
        return builder.build();
    }

    private static String retrieveBuildUrl(Run<?, ?> build) {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        if (rootUrl == null) {
            throw new IllegalStateException("Could not determine Jenkins URL. You should set one in Manage Jenkins.");
        } else {
            return rootUrl + build.getUrl();
        }
    }

    private static String retrieveBuildRevision(Run<?, ?> build) {
        BuildData action = build.getAction(BuildData.class);
        if (action == null) {
            throw new IllegalStateException("No (git-plugin) BuildData associated to current build");
        }
        Revision lastBuiltRevision = action.getLastBuiltRevision();

        if (lastBuiltRevision == null) {
            throw new IllegalStateException("Last build has no associated commit");
        }

        return action.getLastBuild(lastBuiltRevision.getSha1()).getMarked().getSha1String();
    }
}
