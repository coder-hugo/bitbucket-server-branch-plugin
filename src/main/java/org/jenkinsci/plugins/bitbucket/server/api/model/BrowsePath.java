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
public class BrowsePath {

    public enum Type {
        FILE, DIRECTORY
    }

    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrowsePath that = (BrowsePath) o;
        return new EqualsBuilder()
                .append(type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .toString();
    }
}
