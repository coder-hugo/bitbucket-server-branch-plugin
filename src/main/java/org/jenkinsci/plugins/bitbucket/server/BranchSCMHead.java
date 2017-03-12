package org.jenkinsci.plugins.bitbucket.server;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMHead;

/**
 * @author Robin Müller
 */
public class BranchSCMHead extends SCMHead {

    private static final long serialVersionUID = 1L;

    public BranchSCMHead(@NonNull String name) {
        super(name);
    }
}
