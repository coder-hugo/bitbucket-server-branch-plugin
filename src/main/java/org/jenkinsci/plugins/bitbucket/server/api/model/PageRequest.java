package org.jenkinsci.plugins.bitbucket.server.api.model;

import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;

import javax.ws.rs.QueryParam;

/**
 * @author Robin Müller
 */
public class PageRequest {

    @QueryParam("start")
    private final Integer start;

    @QueryParam("limit")
    private final Integer limit;

    public static PageRequest nextPage(Page<?> page) {
        return page == null ? null : new PageRequest(page.getStart() + page.getLimit(), page.getLimit());
    }

    @BitbucketPojoBuilder
    public PageRequest(Integer start, Integer limit) {
        this.start = start;
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getLimit() {
        return limit;
    }
}
