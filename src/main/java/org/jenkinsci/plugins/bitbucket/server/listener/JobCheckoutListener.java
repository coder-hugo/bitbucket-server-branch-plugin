package org.jenkinsci.plugins.bitbucket.server.listener;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;

import javax.annotation.CheckForNull;
import java.io.File;

/**
 * @author Robin Müller
 */
@Extension
public class JobCheckoutListener extends SCMListener {

    @Override
    public void onCheckout(Run<?, ?> build,
                           SCM scm,
                           FilePath workspace,
                           TaskListener listener,
                           @CheckForNull File changelogFile,
                           @CheckForNull SCMRevisionState pollingBaseline) throws Exception {
        BitbucketBuildStatusNotifier.updateBuildStatus(build, listener);
    }
}
