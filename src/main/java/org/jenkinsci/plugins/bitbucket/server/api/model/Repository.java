package org.jenkinsci.plugins.bitbucket.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Robin Müller
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@BitbucketPojoBuilder
public class Repository {

    private String slug;
    private String name;
    private String scmId;
    private Map<String, List<Link>> links;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScmId() {
        return scmId;
    }

    public void setScmId(String scmId) {
        this.scmId = scmId;
    }

    public Map<String, List<Link>> getLinks() {
        return links;
    }

    public void setLinks(Map<String, List<Link>> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("slug", slug)
                .append("name", name)
                .append("scmId", scmId)
                .append("links", links)
                .toString();
    }
}
