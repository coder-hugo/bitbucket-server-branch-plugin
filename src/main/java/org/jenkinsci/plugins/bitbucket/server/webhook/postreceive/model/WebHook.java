package org.jenkinsci.plugins.bitbucket.server.webhook.postreceive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * @author Robin Müller
 *
 * TODO move this to bitbucket-server-post-receive-webhook plugin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebHook {

    private Repository repository;
    private Project project;
    private List<RefChange> refChanges;

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<RefChange> getRefChanges() {
        return refChanges;
    }

    public void setRefChanges(List<RefChange> refChanges) {
        this.refChanges = refChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebHook webHook = (WebHook) o;
        return new EqualsBuilder()
                .append(repository, webHook.repository)
                .append(project, webHook.project)
                .append(refChanges, webHook.refChanges)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(repository)
                .append(project)
                .append(refChanges)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("repository", repository)
                .append("project", project)
                .append("refChanges", refChanges)
                .toString();
    }
}
