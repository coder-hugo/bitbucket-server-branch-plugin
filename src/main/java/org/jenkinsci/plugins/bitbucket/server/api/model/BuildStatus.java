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
public class BuildStatus {

    public enum State {
        INPROGRESS, SUCCESSFUL, FAILED
    }

    private State state;
    private String key;
    private String name;
    private String url;
    private String description;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildStatus that = (BuildStatus) o;
        return new EqualsBuilder()
                .append(state, that.state)
                .append(key, that.key)
                .append(name, that.name)
                .append(url, that.url)
                .append(description, that.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(state)
                .append(key)
                .append(name)
                .append(url)
                .append(description)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("state", state)
                .append("key", key)
                .append("name", name)
                .append("url", url)
                .append("description", description)
                .toString();
    }
}
