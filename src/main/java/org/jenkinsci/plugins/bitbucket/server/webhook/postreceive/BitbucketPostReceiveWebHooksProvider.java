package org.jenkinsci.plugins.bitbucket.server.webhook.postreceive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookEvent;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider;
import org.jenkinsci.plugins.bitbucket.server.webhook.postreceive.model.RefChange;
import org.jenkinsci.plugins.bitbucket.server.webhook.postreceive.model.Repository;
import org.jenkinsci.plugins.bitbucket.server.webhook.postreceive.model.WebHook;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jenkinsci.plugins.bitbucket.server.webhook.api.builder.BitbucketWebHookEventBuilder.aBitbucketWebHookEvent;

/**
 * @author Robin Müller
 *
 * TODO move this to bitbucket-server-post-receive-webhook plugin
 */
@Extension
public class BitbucketPostReceiveWebHooksProvider extends BitbucketWebHookProvider {

    @Nonnull
    @Override
    public String getAddonDisplayName() {
        return "Post-Receive WebHooks";
    }

    @Nonnull
    @Override
    public String getAddonKey() {
        return "com.atlassian.stash.plugin.stash-web-post-receive-hooks-plugin:postReceiveHook";
    }

    @Nonnull
    @Override
    public List<BitbucketWebHookEvent> processWebHook(StaplerRequest request) throws IOException {
        List<BitbucketWebHookEvent> result = new ArrayList<>();
        String body = IOUtils.toString(request.getInputStream());
        WebHook hook = new ObjectMapper().readValue(body, WebHook.class);
        Repository repository = hook.getRepository();
        String project = repository.getProject().getKey();
        for (RefChange refChange : hook.getRefChanges()) {
            String refId = refChange.getRefId();
            String toHash = refChange.getToHash();
            BitbucketWebHookEvent.Type webHookType;
            switch (refChange.getType()) {
                case ADD:
                    webHookType = BitbucketWebHookEvent.Type.CREATE;
                    break;
                case UPDATE:
                    webHookType = BitbucketWebHookEvent.Type.UPDATE;
                    break;
                case DELETE:
                    webHookType = BitbucketWebHookEvent.Type.DELETE;
                    break;
                default:
                    continue;
            }
            result.add(aBitbucketWebHookEvent()
                               .type(webHookType)
                               .project(project)
                               .repository(repository.getName())
                               .branch(refId.replaceFirst("^refs/heads/", ""))
                               .commitId(toHash)
                               .build());
        }
        return result;
    }

    @Override
    public boolean isUrlConfigured(JsonNode currentConfiguration, String hookUrl) {
        for (JsonNode node : currentConfiguration) {
            if (node.asText().equals(hookUrl)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public Map<String, Object> getUpdatedConfiguration(JsonNode currentConfiguration, UpdateAction updateAction, String hookUrl) {
        Map<String, Object> result = new HashMap<>();
        if (currentConfiguration != null) {
            for (JsonNode node : currentConfiguration) {
                String url = node.asText();
                if (url.equals(hookUrl) && updateAction == UpdateAction.DELETE) {
                    continue;
                }
                result.put("hook-url-" + result.size(), url);
            }
        }
        if (updateAction == UpdateAction.ADD && !result.containsValue(hookUrl)) {
            result.put("hook-url-" + result.size(), hookUrl);
        }
        return result;
    }

    @Override
    public boolean canHookBeDisabled(JsonNode currentConfiguration) {
        return currentConfiguration.size() == 0;
    }
}
