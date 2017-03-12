package org.jenkinsci.plugins.bitbucket.server;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMProbe;
import jenkins.scm.api.SCMProbeStat;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.BrowsePath;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientService;

import javax.ws.rs.ClientErrorException;
import java.io.IOException;

/**
 * @author Robin Müller
 */
class BitbucketSCMProbe extends SCMProbe {

    private final SCMHead head;
    private final SCMRevision revision;
    private final BitbucketClientConfiguration clientConfiguration;
    private final SCMSourceOwner context;
    private final String project;
    private final String repository;

    BitbucketSCMProbe(SCMHead head,
                      SCMRevision revision,
                      BitbucketClientConfiguration clientConfiguration,
                      SCMSourceOwner context,
                      String project,
                      String repository) {
        this.head = head;
        this.revision = revision;
        this.clientConfiguration = clientConfiguration;
        this.context = context;
        this.project = project;
        this.repository = repository;
    }

    @Override
    public String name() {
        return head.getName();
    }

    @Override
    public long lastModified() {
        if (revision instanceof AbstractGitSCMSource.SCMRevisionImpl) {
            return getClient().getCommit(project, repository, ((AbstractGitSCMSource.SCMRevisionImpl) revision).getHash()).getAuthorTimestamp();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public SCMProbeStat stat(@NonNull String path) throws IOException {
        try {
            BrowsePath browsePath = getClient().browse(project, repository, path, head.getName(), true);
            switch (browsePath.getType()) {
                case FILE:
                    return SCMProbeStat.fromType(SCMFile.Type.REGULAR_FILE);
                case DIRECTORY:
                    return SCMProbeStat.fromType(SCMFile.Type.DIRECTORY);
                default:
                    return SCMProbeStat.fromType(SCMFile.Type.NONEXISTENT);
            }
        } catch (ClientErrorException e) {
            return SCMProbeStat.fromType(SCMFile.Type.NONEXISTENT);
        }
    }

    @Override
    public void close() throws IOException {

    }

    private BitbucketServerAPI getClient() {
        return BitbucketServerClientService.instance().getClient(clientConfiguration, context);
    }
}
