package org.jenkinsci.plugins.bitbucket.server.client;

import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.Branch;
import org.jenkinsci.plugins.bitbucket.server.api.model.Page;
import org.jenkinsci.plugins.bitbucket.server.api.model.PageRequest;
import org.jenkinsci.plugins.bitbucket.server.api.model.Repository;

/**
 * @author Robin Müller
 */
public class BitbucketPagingClient {

    private final BitbucketServerAPI client;

    public BitbucketPagingClient(BitbucketServerAPI client) {
        this.client = client;
    }

    public Iterable<Repository> getRepositories(final String project) {
        return new APIPageIterable<Repository>() {
            @Override
            protected Page<Repository> getNextPage(PageRequest pageRequest) {
                return client.getRepositories(project, pageRequest);
            }
        };
    }

    public Iterable<Branch> getBranches(final String project, final String repository) {
        return new APIPageIterable<Branch>() {
            @Override
            protected Page<Branch> getNextPage(PageRequest pageRequest) {
                return client.getBranches(project, repository, pageRequest);
            }
        };
    }
}
