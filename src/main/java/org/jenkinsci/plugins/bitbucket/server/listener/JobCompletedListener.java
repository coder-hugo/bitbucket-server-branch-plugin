package org.jenkinsci.plugins.bitbucket.server.listener;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

/**
 * @author Robin Müller
 */
@Extension
public class JobCompletedListener extends RunListener<Run<?, ?>> {

    @Override
    public void onCompleted(Run<?, ?> run, @Nonnull TaskListener listener) {
        BitbucketBuildStatusNotifier.updateBuildStatus(run, listener);
    }
}
