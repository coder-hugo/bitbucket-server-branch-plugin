package org.jenkinsci.plugins.bitbucket.server.webhook;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider;

import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.bitbucket.server.webhook.builder.WebHookAddonBuilder.aWebHookAddon;

/**
 * @author Robin Müller
 */
public final class WebHookAddonResolver {

    private WebHookAddonResolver() {
    }

    public static List<WebHookAddon> getWebHookAddons() {
        List<WebHookAddon> result = new ArrayList<>();
        for (BitbucketWebHookProvider provider : Jenkins.getActiveInstance().getExtensionList(BitbucketWebHookProvider.class)) {
            result.add(aWebHookAddon()
                               .name(provider.getAddonDisplayName())
                               .url(provider.getWebHookUrl(BitbucketWebhook.PATH + "/" + provider.getAddonKey() + "/"))
                               .build());
        }
        return result;
    }
}
