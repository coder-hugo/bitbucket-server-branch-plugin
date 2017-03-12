package org.jenkinsci.plugins.bitbucket.server.client;

import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;

/**
 * @author Robin Müller
 */
public class BitbucketServerClientBuilderMockUtils {

    public static void putClient(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context, BitbucketServerAPI client) {
        BitbucketServerClientService.instance().put(clientConfiguration, context, client);
    }
}
