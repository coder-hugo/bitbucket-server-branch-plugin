package org.jenkinsci.plugins.bitbucket.server.listener;

import com.fasterxml.jackson.databind.JsonNode;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import hudson.triggers.SafeTimerTask;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.BitbucketSCMSource;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;
import org.jenkinsci.plugins.bitbucket.server.api.model.HookAddon;
import org.jenkinsci.plugins.bitbucket.server.client.BitbucketPagingClient;
import org.jenkinsci.plugins.bitbucket.server.webhook.BitbucketWebhook;
import org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider.UpdateAction.ADD;
import static org.jenkinsci.plugins.bitbucket.server.webhook.api.BitbucketWebHookProvider.UpdateAction.DELETE;

/**
 * @author Robin Müller
 */
@Extension
public class WebhookAutoRegisterListener extends ItemListener {

    private static final Logger LOGGER = Logger.getLogger(WebhookAutoRegisterListener.class.getName());

    private static ExecutorService executorService;

    @Override
    public void onCreated(Item item) {
        if (!isApplicable(item)) {
            return;
        }
        registerHooksAsync((SCMSourceOwner) item);
    }

    @Override
    public void onDeleted(Item item) {
        if (!isApplicable(item)) {
            return;
        }
        removeHooksAsync((SCMSourceOwner) item);
    }

    @Override
    public void onUpdated(Item item) {
        if (!isApplicable(item)) {
            return;
        }
        registerHooksAsync((SCMSourceOwner) item);
    }

    private boolean isApplicable(Item item) {
        return item instanceof SCMSourceOwner;
    }

    private void registerHooksAsync(final SCMSourceOwner owner) {
        getExecutorService().submit(new SafeTimerTask() {
            @Override
            public void doRun() {
                registerHooks(owner);
            }
        });
    }

    private void removeHooksAsync(final SCMSourceOwner owner) {
        getExecutorService().submit(new SafeTimerTask() {
            @Override
            public void doRun() {
                removeHooks(owner);
            }
        });
    }

    // synchronized just to avoid duplicated webhooks in case SCMSourceOwner is updated repeteadly and quickly
    private synchronized void registerHooks(SCMSourceOwner owner) {
        Map<String, BitbucketWebHookProvider> providers = getBitbucketWebHookProviders();
        String rootUrl = Jenkins.getActiveInstance().getRootUrl();
        if (rootUrl != null && !rootUrl.startsWith("http://localhost")) {
            List<BitbucketSCMSource> sources = getBitucketSCMSources(owner);
            for (BitbucketSCMSource source : sources) {
                if (source.isAutoRegisterHook()) {
                    BitbucketServerAPI client = source.getClient();
                    String project = source.getProject();
                    String repository = source.getRepository();
                    try {
                        for (HookAddon hookAddon : new BitbucketPagingClient(client).getHooks(project, repository)) {
                            String addonKey = hookAddon.getDetails().getKey();
                            String hookUrl = rootUrl + "/" + BitbucketWebhook.PATH + "/" + addonKey;
                            if (providers.containsKey(addonKey)) {
                                JsonNode hookSettings = client.getHookSettings(project, repository, addonKey);
                                BitbucketWebHookProvider provider = providers.get(addonKey);
                                JsonNode currentSettings = null;
                                if (hookAddon.isEnabled()) {
                                    if (provider.isUrlConfigured(hookSettings, hookUrl)) {
                                        break;
                                    } else {
                                        currentSettings = hookSettings;
                                    }
                                }
                                client.updateHookSettings(project, repository, addonKey, provider.getUpdatedConfiguration(currentSettings, ADD, hookUrl));
                                client.enableHook(project, repository, addonKey);
                                break;
                            }
                        }
                    } catch (NotFoundException e) {
                        LOGGER.log(Level.FINE, "Project (%s) or repository (%s) doesn't exist anymore", new Object[]{project, repository});
                    }
                }
            }
        } else {
            LOGGER.warning(String.format("Can not register hook. Jenkins root URL is not valid: %s", rootUrl));
        }
    }

    private void removeHooks(SCMSourceOwner owner) {
        Map<String, BitbucketWebHookProvider> providers = getBitbucketWebHookProviders();
        List<BitbucketSCMSource> sources = getBitucketSCMSources(owner);
        for (BitbucketSCMSource source : sources) {
            if (source.isAutoRegisterHook()) {
                BitbucketServerAPI client = source.getClient();
                String project = source.getProject();
                String repository = source.getRepository();
                for (HookAddon hookAddon : new BitbucketPagingClient(client).getHooks(project, repository)) {
                    String addonKey = hookAddon.getDetails().getKey();
                    if (providers.containsKey(addonKey)) {
                        String hookUrl = Jenkins.getActiveInstance().getRootUrl() + "/" + BitbucketWebhook.PATH + "/" + addonKey;
                        JsonNode hookSettings = client.getHookSettings(project, repository, addonKey);
                        BitbucketWebHookProvider provider = providers.get(addonKey);
                        if (hookAddon.isEnabled() && provider.isUrlConfigured(hookSettings, hookUrl)) {
                            Object newHookSettings = provider.getUpdatedConfiguration(hookSettings, DELETE, hookUrl);
                            JsonNode updatedHookSettings = client.updateHookSettings(project, repository, addonKey, newHookSettings);
                            if (provider.canHookBeDisabled(updatedHookSettings)) {
                                client.disableHook(project, repository, addonKey);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, BitbucketWebHookProvider> getBitbucketWebHookProviders() {
        Map<String, BitbucketWebHookProvider> result = new HashMap<>();
        for (BitbucketWebHookProvider provider : Jenkins.getActiveInstance().getExtensionList(BitbucketWebHookProvider.class)) {
            result.put(provider.getAddonKey(), provider);
        }
        return result;
    }

    private List<BitbucketSCMSource> getBitucketSCMSources(SCMSourceOwner owner) {
        List<BitbucketSCMSource> sources = new ArrayList<>();
        for (SCMSource source : owner.getSCMSources()) {
            if (source instanceof BitbucketSCMSource) {
                sources.add((BitbucketSCMSource) source);
            }
        }
        return sources;
    }

    /**
     * We need a single thread executor to run webhooks operations in background but in order.
     * Registrations and removals need to be done in the same order than they were called by the item listener.
     */
    private static synchronized ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), WebhookAutoRegisterListener.class.getName()));
        }
        return executorService;
    }
}
