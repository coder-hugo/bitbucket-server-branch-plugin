package org.jenkinsci.plugins.bitbucket.server.webhook.api;

import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Nonnull;

/**
 * @author Robin Müller
 *
 * TODO move this to bitbucket-server-webhook-api plugin
 */
public class BitbucketWebHookEvent {

    public enum Type {
        CREATE, UPDATE, DELETE
    }

    @Nonnull private final Type type;
    @Nonnull private final String project;
    @Nonnull private final String repository;
    @Nonnull private final String branch;
    private final String commitId;

    @GeneratePojoBuilder(intoPackage = "*.builder", withFactoryMethod = "a*", withSetterNamePattern = "*")
    public BitbucketWebHookEvent(@Nonnull Type type, @Nonnull String project, @Nonnull String repository, @Nonnull String branch, String commitId) {
        this.type = type;
        this.project = project;
        this.repository = repository;
        this.branch = branch;
        this.commitId = commitId;
    }

    @Nonnull
    public Type getType() {
        return type;
    }

    @Nonnull
    public String getProject() {
        return project;
    }

    @Nonnull
    public String getRepository() {
        return repository;
    }

    @Nonnull
    public String getBranch() {
        return branch;
    }

    public String getCommitId() {
        return commitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BitbucketWebHookEvent that = (BitbucketWebHookEvent) o;
        return new EqualsBuilder()
                .append(type, that.type)
                .append(project, that.project)
                .append(repository, that.repository)
                .append(branch, that.branch)
                .append(commitId, that.commitId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(project)
                .append(repository)
                .append(branch)
                .append(commitId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("project", project)
                .append("repository", repository)
                .append("branch", branch)
                .append("commitId", commitId)
                .toString();
    }
}
