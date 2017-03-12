package org.jenkinsci.plugins.bitbucket.server;

import hudson.util.LogTaskListener;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.Link;
import org.jenkinsci.plugins.bitbucket.server.api.model.Repository;
import org.jenkinsci.plugins.bitbucket.server.api.model.builder.PageBuilder;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketClientConfiguration;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketServerClientBuilderMockUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.bitbucket.server.api.model.builder.RepositoryBuilder.aRepository;
import static org.jenkinsci.plugins.bitbucket.server.client.builder.BitbucketClientConfigurationBuilder.aBitbucketClientConfiguration;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Robin Müller
 */
@RunWith(MockitoJUnitRunner.class)
public class BitbucketSCMNavigatorTest {

    @Mock
    private BitbucketServerAPI clientMock;

    @Mock
    private SCMSourceOwner context;

    private BitbucketClientConfiguration clientConfiguration = aBitbucketClientConfiguration().build();

    @Before
    public void setup() {
        BitbucketServerClientBuilderMockUtils.putClient(clientConfiguration, context, clientMock);
    }

    @Test
    public void visitSources_noRepositories() throws IOException, InterruptedException {
        SCMSourceObserver observer = mock(SCMSourceObserver.class);
        String project = "test";
        when(clientMock.getRepositories(project, null)).thenReturn(PageBuilder.<Repository>aPage()
                                                                           .isLastPage(true)
                                                                           .values(Collections.<Repository>emptyList())
                                                                           .build());
        when(observer.getListener()).thenReturn(new LogTaskListener(Logger.getLogger("test"), Level.FINE));
        when(observer.getContext()).thenReturn(context);

        BitbucketSCMNavigator scmNavigator = new BitbucketSCMNavigator(clientConfiguration, project);
        scmNavigator.visitSources(observer);

        verify(observer, never()).observe(anyString());
    }

    @Test
    public void visitSources_withRepositories() throws IOException, InterruptedException {
        SCMSourceObserver observer = mock(SCMSourceObserver.class);
        SCMSourceObserver.ProjectObserver projectObserver = mock(SCMSourceObserver.ProjectObserver.class);
        String project = "test";
        Repository repository1 = aRepository()
                .name("Repository 1")
                .slug("repo1")
                .build();
        Repository repository2 = aRepository()
                .name("Repository 2")
                .slug("repo2")
                .build();
        when(clientMock.getRepositories(project, null)).thenReturn(PageBuilder.<Repository>aPage()
                                                                           .isLastPage(true)
                                                                           .values(Arrays.asList(repository1, repository2))
                                                                           .build());
        when(observer.getListener()).thenReturn(new LogTaskListener(Logger.getLogger("test"), Level.FINE));
        when(observer.observe(anyString())).thenReturn(projectObserver);
        when(observer.getContext()).thenReturn(context);

        BitbucketSCMNavigator scmNavigator = new BitbucketSCMNavigator(clientConfiguration, project);
        scmNavigator.visitSources(observer);

        verify(observer, times(1)).observe(repository1.getName());
        verify(projectObserver, times(1)).addSource(new BitbucketSCMSource(BitbucketSCMNavigator.class.getName() + "::null::test::" + repository1.getSlug(),
                                                                           clientConfiguration,
                                                                           project,
                                                                           repository1.getSlug()));
        verify(observer, times(1)).observe(repository2.getName());
        verify(projectObserver, times(1)).addSource(new BitbucketSCMSource(BitbucketSCMNavigator.class.getName() + "::null::test::" + repository2.getSlug(),
                                                                           clientConfiguration,
                                                                           project,
                                                                           repository2.getSlug()));
    }

    @Test
    public void visitSources_withRepositories_nonMatchingPattern() throws IOException, InterruptedException {
        SCMSourceObserver observer = mock(SCMSourceObserver.class);
        SCMSourceObserver.ProjectObserver projectObserver = mock(SCMSourceObserver.ProjectObserver.class);
        String project = "test";
        Repository repository = aRepository()
                .name("Repository")
                .slug("repo")
                .links(Collections.<String, List<Link>>emptyMap())
                .build();
        when(clientMock.getRepositories(project, null)).thenReturn(PageBuilder.<Repository>aPage()
                                                                           .isLastPage(true)
                                                                           .values(Collections.singletonList(repository))
                                                                           .build());
        when(observer.getListener()).thenReturn(new LogTaskListener(Logger.getLogger("test"), Level.FINE));
        when(observer.observe(anyString())).thenReturn(projectObserver);
        when(observer.getContext()).thenReturn(context);

        BitbucketSCMNavigator scmNavigator = new BitbucketSCMNavigator(clientConfiguration, project);
        scmNavigator.setPattern("test");
        scmNavigator.visitSources(observer);

        verify(observer, never()).observe(repository.getName());
    }
}
