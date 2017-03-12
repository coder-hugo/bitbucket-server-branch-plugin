package org.jenkinsci.plugins.bitbucket.server.webhook.api;

import com.fasterxml.jackson.databind.JsonNode;
import hudson.ExtensionPoint;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * @author Robin Müller
 *
 * TODO move this to bitbucket-server-webhook-api plugin
 */
public abstract class BitbucketWebHookProvider implements ExtensionPoint {

    public final String getWebHookUrl(String baseUrl) {
        return baseUrl + getAddonUrlSuffix();
    }

    @Nonnull
    public abstract String getAddonDisplayName();

    @Nonnull
    public abstract String getAddonKey();

    @Nonnull
    public abstract List<BitbucketWebHookEvent> processWebHook(StaplerRequest request) throws IOException;

    public abstract boolean isUrlConfigured(JsonNode currentConfiguration, String hookUrl);

    @Nonnull
    public abstract Object getUpdatedConfiguration(JsonNode currentConfiguration, UpdateAction updateAction, String hookUrl);

    public abstract boolean canHookBeDisabled(JsonNode currentConfiguration);

    /**
     * Override this if the Bitbucket Server WebHook add-on adds a suffix to the configured URL.
     *
     * @return an add-on specific URL (if applicable)
     */
    protected String getAddonUrlSuffix() {
        return "";
    }


    public enum UpdateAction {
        ADD, DELETE
    }
}
