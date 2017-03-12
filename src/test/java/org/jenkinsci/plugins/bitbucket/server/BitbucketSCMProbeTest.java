package org.jenkinsci.plugins.bitbucket.server;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMHead;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.BrowsePath;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientBuilderMockUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.ClientErrorException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.BrowsePathBuilder.aBrowsePath;
import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.CommitBuilder.aCommit;
import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Robin Müller
 */
@RunWith(MockitoJUnitRunner.class)
public class BitbucketSCMProbeTest {

    @Mock
    private BitbucketServerAPI clientMock;

    private BitbucketClientConfiguration clientConfiguration = aBitbucketClientConfiguration().build();

    @Before
    public void setup() {
        BitbucketServerClientBuilderMockUtils.putClient(clientConfiguration, null, clientMock);
    }

    @Test
    public void stat_isFile() throws IOException {
        String branch = "master";
        String project = "project";
        String repository = "repository";
        String path = "Jenkinsfile";

        when(clientMock.browse(project, repository, path, branch, true)).thenReturn(aBrowsePath().type(BrowsePath.Type.FILE).build());

        BitbucketSCMProbe probe = new BitbucketSCMProbe(new SCMHead(branch), null, clientConfiguration, null, project, repository);

        assertThat(probe.stat(path).getType(), is(SCMFile.Type.REGULAR_FILE));
    }

    @Test
    public void stat_isDirectory() throws IOException {
        String branch = "master";
        String project = "project";
        String repository = "repository";
        String path = "src";

        when(clientMock.browse(project, repository, path, branch, true)).thenReturn(aBrowsePath().type(BrowsePath.Type.DIRECTORY).build());

        BitbucketSCMProbe probe = new BitbucketSCMProbe(new SCMHead(branch), null, clientConfiguration, null, project, repository);

        assertThat(probe.stat(path).getType(), is(SCMFile.Type.DIRECTORY));
    }

    @Test
    public void stat_doesNotExist() throws IOException {
        String branch = "master";
        String project = "project";
        String repository = "repository";
        String path = "Jenkinsfile";

        when(clientMock.browse(project, repository, path, branch, true)).thenThrow(ClientErrorException.class);

        BitbucketSCMProbe probe = new BitbucketSCMProbe(new SCMHead(branch), null, clientConfiguration, null, project, repository);

        assertThat(probe.stat(path).exists(), is(false));
    }

    @Test
    public void lastModified() {
        String branch = "master";
        String project = "project";
        String repository = "repository";
        String hash = "123abc";
        long timestamp = 123L;

        when(clientMock.getCommit(project, repository, hash)).thenReturn(aCommit().authorTimestamp(timestamp).build());

        SCMHead head = new SCMHead(branch);
        BitbucketSCMProbe probe = new BitbucketSCMProbe(head, new AbstractGitSCMSource.SCMRevisionImpl(head, hash), clientConfiguration, null, project, repository);

        assertThat(probe.lastModified(), is(timestamp));
    }
}
