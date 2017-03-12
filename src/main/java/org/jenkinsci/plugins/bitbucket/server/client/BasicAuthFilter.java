package org.jenkinsci.plugins.bitbucket.server.client;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * @author Robin Müller
 */
@Priority(Priorities.HEADER_DECORATOR)
class BasicAuthFilter implements ClientRequestFilter {

    private final StandardUsernamePasswordCredentials credentials;

    BasicAuthFilter(StandardUsernamePasswordCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (credentials != null) {
            requestContext.getHeaders().add("Authorization", getBasicAuthentication());
        }
    }

    private String getBasicAuthentication() {
        String token = credentials.getUsername() + ":" + credentials.getPassword().getPlainText();
        return "Basic " + Base64.encodeBase64String(token.getBytes());
    }
}
