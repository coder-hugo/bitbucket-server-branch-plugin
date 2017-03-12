package org.jenkinsci.plugins.bitbucket.server.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.jenkinsci.plugins.bitbucket.server.api.model.Branch;
import org.jenkinsci.plugins.bitbucket.server.api.model.BrowsePath;
import org.jenkinsci.plugins.bitbucket.server.api.model.BuildStatus;
import org.jenkinsci.plugins.bitbucket.server.api.model.Commit;
import org.jenkinsci.plugins.bitbucket.server.api.model.HookAddon;
import org.jenkinsci.plugins.bitbucket.server.api.model.Page;
import org.jenkinsci.plugins.bitbucket.server.api.model.PageRequest;
import org.jenkinsci.plugins.bitbucket.server.api.model.Repository;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Robin Müller
 */
@Path("/rest")
public interface BitbucketServerAPI {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos")
    Page<Repository> getRepositories(@PathParam("project") String project,
                                     @BeanParam PageRequest pageRequest);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}")
    Repository getRepository(@PathParam("project") String project,
                             @PathParam("repositorySlug") String repositorySlug);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/branches")
    Page<Branch> getBranches(@PathParam("project") String project,
                             @PathParam("repositorySlug") String repositorySlug,
                             @BeanParam PageRequest pageRequest);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/commits/{commitId}")
    Commit getCommit(@PathParam("project") String project,
                     @PathParam("repositorySlug") String repositorySlug,
                     @PathParam("commitId") String commitId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/browse/{path}")
    BrowsePath browse(@PathParam("project") String project,
                      @PathParam("repositorySlug") String repositorySlug,
                      @PathParam("path") String path,
                      @QueryParam("at") String at,
                      @QueryParam("type") boolean type);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/settings/hooks")
    Page<HookAddon> getHooks(@PathParam("project") String project,
                             @PathParam("repositorySlug") String repositorySlug,
                             @BeanParam PageRequest pageRequest);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/settings/hooks/{key}/enabled")
    HookAddon enableHook(@PathParam("project") String project,
                    @PathParam("repositorySlug") String repositorySlug,
                    @PathParam("key") String key);

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/settings/hooks/{key}/enabled")
    HookAddon disableHook(@PathParam("project") String project,
                     @PathParam("repositorySlug") String repositorySlug,
                     @PathParam("key") String key);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/settings/hooks/{key}/settings")
    JsonNode getHookSettings(@PathParam("project") String project,
                             @PathParam("repositorySlug") String repositorySlug,
                             @PathParam("key") String key);


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/api/1.0/projects/{project}/repos/{repositorySlug}/settings/hooks/{key}/settings")
    JsonNode updateHookSettings(@PathParam("project") String project,
                                           @PathParam("repositorySlug") String repositorySlug,
                                           @PathParam("key") String key,
                                           Object hookSettings);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/build-status/1.0/commits/{commitId}")
    void updateBuildStatus(@PathParam("commitId") String commitId,
                           BuildStatus buildStatus);
}
