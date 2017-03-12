package org.jenkinsci.plugins.bitbucket.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;

/**
 * @author Robin Müller
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@BitbucketPojoBuilder
public class Commit {

    private String id;
    private String displayId;
    private Long authorTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    public Long getAuthorTimestamp() {
        return authorTimestamp;
    }

    public void setAuthorTimestamp(Long authorTimestamp) {
        this.authorTimestamp = authorTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Commit commit = (Commit) o;
        return new EqualsBuilder()
                .append(id, commit.id)
                .append(displayId, commit.displayId)
                .append(authorTimestamp, commit.authorTimestamp)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(displayId)
                .append(authorTimestamp)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("displayId", displayId)
                .append("authorTimestamp", authorTimestamp)
                .toString();
    }
}
