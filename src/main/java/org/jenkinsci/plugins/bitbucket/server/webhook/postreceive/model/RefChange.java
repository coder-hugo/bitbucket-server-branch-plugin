package org.jenkinsci.plugins.bitbucket.server.webhook.postreceive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author Robin Müller
 *
 * TODO move this to bitbucket-server-post-receive-webhook plugin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefChange {

    public enum Type {
        ADD, UPDATE, DELETE
    }

    private Type type;
    private String refId;
    private String toHash;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getToHash() {
        return toHash;
    }

    public void setToHash(String toHash) {
        this.toHash = toHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefChange refChange = (RefChange) o;
        return new EqualsBuilder()
                .append(type, refChange.type)
                .append(refId, refChange.refId)
                .append(toHash, refChange.toHash)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(refId)
                .append(toHash)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("refId", refId)
                .append("toHash", toHash)
                .toString();
    }
}
