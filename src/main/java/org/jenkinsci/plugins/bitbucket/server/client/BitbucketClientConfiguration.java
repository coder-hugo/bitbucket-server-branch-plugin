package org.jenkinsci.plugins.bitbucket.server.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;

/**
 * @author Robin Müller
 */
public class BitbucketClientConfiguration {

    private final String baseUrl;
    private final String credentialsId;

    @BitbucketPojoBuilder
    public BitbucketClientConfiguration(String baseUrl, String credentialsId) {
        this.baseUrl = baseUrl;
        this.credentialsId = credentialsId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BitbucketClientConfiguration that = (BitbucketClientConfiguration) o;
        return new EqualsBuilder()
                .append(baseUrl, that.baseUrl)
                .append(credentialsId, that.credentialsId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(baseUrl)
                .append(credentialsId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("baseUrl", baseUrl)
                .append("credentialsId", credentialsId)
                .toString();
    }
}
