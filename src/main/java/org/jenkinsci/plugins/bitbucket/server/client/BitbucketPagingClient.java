package org.jenkinsci.plugins.bitbucket.server.client;

import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.Branch;
import org.jenkinsci.plugins.bitbucket.server.api.model.HookAddon;
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
                Integer start = pageRequest == null ? null : pageRequest.getStart();
                Integer limit = pageRequest == null ? null : pageRequest.getLimit();
                return client.getRepositories(project, start, limit);
            }
        };
    }

    public Iterable<Branch> getBranches(final String project, final String repository) {
        return new APIPageIterable<Branch>() {
            @Override
            protected Page<Branch> getNextPage(PageRequest pageRequest) {
                Integer start = pageRequest == null ? null : pageRequest.getStart();
                Integer limit = pageRequest == null ? null : pageRequest.getLimit();
                return client.getBranches(project, repository, start, limit);
            }
        };
    }

    public Iterable<HookAddon> getHooks(final String project, final String repository) {
        return new APIPageIterable<HookAddon>() {
            @Override
            protected Page<HookAddon> getNextPage(PageRequest pageRequest) {
                Integer start = pageRequest == null ? null : pageRequest.getStart();
                Integer limit = pageRequest == null ? null : pageRequest.getLimit();
                return client.getHooks(project, repository, start, limit);
            }
        };
    }
}
