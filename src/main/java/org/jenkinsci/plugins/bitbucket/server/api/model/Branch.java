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
public class Branch {

    private String id;
    private String displayId;
    private String latestCommit;
    private Boolean isDefault;

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

    public String getLatestCommit() {
        return latestCommit;
    }

    public void setLatestCommit(String latestCommit) {
        this.latestCommit = latestCommit;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Branch branch = (Branch) o;
        return new EqualsBuilder()
                .append(id, branch.id)
                .append(displayId, branch.displayId)
                .append(latestCommit, branch.latestCommit)
                .append(isDefault, branch.isDefault)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(displayId)
                .append(latestCommit)
                .append(isDefault)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("displayId", displayId)
                .append("lastCommit", latestCommit)
                .append("isDefault", isDefault)
                .toString();
    }
}
