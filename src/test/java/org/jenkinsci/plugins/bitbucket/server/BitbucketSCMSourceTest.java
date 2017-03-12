package org.jenkinsci.plugins.bitbucket.server;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.Branch;
import org.jenkinsci.plugins.bitbucket.server.api.model.BrowsePath;
import org.jenkinsci.plugins.bitbucket.server.api.model.Link;
import org.jenkinsci.plugins.bitbucket.server.api.model.builder.PageBuilder;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientBuilderMockUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.ClientErrorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.BranchBuilder.aBranch;
import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.BrowsePathBuilder.aBrowsePath;
import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.LinkBuilder.aLink;
import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Robin Müller
 */
@RunWith(MockitoJUnitRunner.class)
public class BitbucketSCMSourceTest {

    @Mock
    private BitbucketServerAPI clientMock;

    @Mock
    private SCMSourceOwner context;

    @ClassRule
    public static JenkinsRule jenkins = new JenkinsRule();

    private BitbucketClientConfiguration clientConfiguration = aBitbucketClientConfiguration().build();

    @Before
    public void setup() {
        BitbucketServerClientBuilderMockUtils.putClient(clientConfiguration, context, clientMock);
    }

    @Test
    @WithoutJenkins
    public void retrieve_noBranches_noCriteria() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Collections.<Branch>emptyList())
                                                                                   .build());
        when(observer.getIncludes()).thenReturn(null);
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(null, observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        verify(observer, never()).observe(any(SCMHead.class), any(SCMRevision.class));
    }

    @Test
    @WithoutJenkins
    public void retrieve_noBranches_withCriteria() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Collections.<Branch>emptyList())
                                                                                   .build());
        when(observer.getIncludes()).thenReturn(null);
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(new JenkinsfileCriteria(), observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        verify(observer, never()).observe(any(SCMHead.class), any(SCMRevision.class));
    }

    @Test
    @WithoutJenkins
    public void retrieve_withBranches_noCriteria() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";
        String branch = "master";
        String commitId = "123abc";

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Collections.singletonList(aBranch()
                                                                                                                             .displayId(branch)
                                                                                                                             .latestCommit(commitId)
                                                                                                                             .build()))
                                                                                   .build());
        when(observer.getIncludes()).thenReturn(null);
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(null, observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        SCMHead head = new BranchSCMHead(branch);
        verify(observer, times(1)).observe(head, new AbstractGitSCMSource.SCMRevisionImpl(head, commitId));
    }

    @Test
    @WithoutJenkins
    public void retrieve_withBranches_withCriteria_noMatch() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";
        String branch = "master";
        String commitId = "123abc";

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Collections.singletonList(aBranch()
                                                                                                                             .displayId(branch)
                                                                                                                             .latestCommit(commitId)
                                                                                                                             .build()))
                                                                                   .build());
        when(clientMock.browse(project, repository, "Jenkinsfile", branch, true)).thenThrow(ClientErrorException.class);
        when(observer.getIncludes()).thenReturn(null);
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(new JenkinsfileCriteria(), observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        verify(observer, never()).observe(any(SCMHead.class), any(SCMRevision.class));
    }

    @Test
    @WithoutJenkins
    public void retrieve_withBranches_withCriteria_withMatch() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";
        Branch matchingBranch = aBranch()
                .displayId("master")
                .latestCommit("123abc")
                .build();
        Branch nonMatchingBranch = aBranch()
                .displayId("no-jenkins-file")
                .latestCommit("456def")
                .build();

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Arrays.asList(matchingBranch, nonMatchingBranch))
                                                                                   .build());
        when(clientMock.browse(project, repository, "Jenkinsfile", matchingBranch.getDisplayId(), true)).thenReturn(aBrowsePath()
                                                                                                                            .type(BrowsePath.Type.FILE)
                                                                                                                            .build());
        when(clientMock.browse(project, repository, "Jenkinsfile", nonMatchingBranch.getDisplayId(), true)).thenThrow(ClientErrorException.class);
        when(observer.getIncludes()).thenReturn(null);
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(new JenkinsfileCriteria(), observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        SCMHead matchingHead = new BranchSCMHead(matchingBranch.getDisplayId());
        verify(observer, times(1)).observe(matchingHead, new AbstractGitSCMSource.SCMRevisionImpl(matchingHead, matchingBranch.getLatestCommit()));
        SCMHead nonMatchingHead = new BranchSCMHead(nonMatchingBranch.getDisplayId());
        verify(observer, never()).observe(nonMatchingHead, new AbstractGitSCMSource.SCMRevisionImpl(nonMatchingHead, nonMatchingBranch.getLatestCommit()));
    }

    @Test
    @WithoutJenkins
    public void retrieve_observeOnlyIncluded() throws IOException, InterruptedException {
        SCMHeadObserver observer = mock(SCMHeadObserver.class);
        String project = "project";
        String repository = "repository";
        String includedBranch = "master";
        String includedCommitId = "123abc";
        String notIncludedBranch = "not-included";
        String notIncludedCommitId = "456def";

        when(clientMock.getBranches(project, repository, null)).thenReturn(PageBuilder.<Branch>aPage()
                                                                                   .isLastPage(true)
                                                                                   .values(Arrays.asList(aBranch()
                                                                                                                 .displayId(includedBranch)
                                                                                                                 .latestCommit(includedCommitId)
                                                                                                                 .build(),
                                                                                                         aBranch()
                                                                                                                 .displayId(notIncludedBranch)
                                                                                                                 .latestCommit(notIncludedCommitId)
                                                                                                                 .build()))
                                                                                   .build());
        when(observer.getIncludes()).thenReturn(Collections.<SCMHead>singleton(new BranchSCMHead(includedBranch)));
        when(observer.isObserving()).thenReturn(true);

        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setOwner(context);

        scmSource.retrieve(null, observer, null, new LogTaskListener(Logger.getLogger("test"), Level.FINE));

        SCMHead includedHead = new BranchSCMHead(includedBranch);
        verify(observer, times(1)).observe(includedHead, new AbstractGitSCMSource.SCMRevisionImpl(includedHead, includedCommitId));

        SCMHead notIncludedHead = new BranchSCMHead(notIncludedBranch);
        verify(observer, never()).observe(notIncludedHead, new AbstractGitSCMSource.SCMRevisionImpl(notIncludedHead, notIncludedCommitId));
    }

    @Test
    public void retrieveCloneUrl_anonymous() {
        String project = "project";
        String repository = "repository";
        String sshUrl = "ssh://git@bitbucket-server/project/repository.git";
        String httpUrl = "https://jenkins@bitbucket-server/scm/PROJECT/repository.git";

        HashMap<String, List<Link>> links = new HashMap<>();
        links.put("clone", Arrays.asList(aLink()
                                                 .name("ssh")
                                                 .href(sshUrl)
                                                 .build(),
                                         aLink()
                                                 .name("http")
                                                 .href(httpUrl)
                                                 .build()));
        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);

        assertThat(scmSource.retrieveCloneUrl(links), is(httpUrl));
    }

    @Test
    public void retrieveCloneUrl_httpCredentials() throws IOException {
        String project = "project";
        String repository = "repository";
        String sshUrl = "ssh://git@bitbucket-server/project/repository.git";
        String httpUrl = "https://jenkins@bitbucket-server/scm/PROJECT/repository.git";
        String credentialsId = "http-credentials";
        SystemCredentialsProvider credentialsProvider = SystemCredentialsProvider.getInstance();
        credentialsProvider.getCredentials().add(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialsId, "", "", ""));
        credentialsProvider.save();

        HashMap<String, List<Link>> links = new HashMap<>();
        links.put("clone", Arrays.asList(aLink()
                                                 .name("ssh")
                                                 .href(sshUrl)
                                                 .build(),
                                         aLink()
                                                 .name("http")
                                                 .href(httpUrl)
                                                 .build()));
        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setCheckoutCredentialsId(credentialsId);

        assertThat(scmSource.retrieveCloneUrl(links), is(httpUrl));
    }

    @Test
    public void retrieveCloneUrl_sshCredentials() throws IOException {
        String project = "project";
        String repository = "repository";
        String sshUrl = "ssh://git@bitbucket-server/project/repository.git";
        String httpUrl = "https://jenkins@bitbucket-server/scm/PROJECT/repository.git";
        String credentialsId = "ssh-credentials";
        SystemCredentialsProvider credentialsProvider = SystemCredentialsProvider.getInstance();
        credentialsProvider.getCredentials().add(new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL, credentialsId, "",
                                                                            new FileOnMasterPrivateKeySource(""), "", ""));
        credentialsProvider.save();

        HashMap<String, List<Link>> links = new HashMap<>();
        links.put("clone", Arrays.asList(aLink()
                                                 .name("ssh")
                                                 .href(sshUrl)
                                                 .build(),
                                         aLink()
                                                 .name("http")
                                                 .href(httpUrl)
                                                 .build()));
        BitbucketSCMSource scmSource = new BitbucketSCMSource(null, clientConfiguration, project, repository);
        scmSource.setCheckoutCredentialsId(credentialsId);

        assertThat(scmSource.retrieveCloneUrl(links), is(sshUrl));
    }

    private static class JenkinsfileCriteria implements SCMSourceCriteria {
        @Override
        public boolean isHead(@NonNull Probe probe, @NonNull TaskListener listener) throws IOException {
            return probe.stat("Jenkinsfile").exists();
        }
    }
}
