package org.jenkinsci.plugins.bitbucket.server.client;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import jenkins.scm.api.SCMSourceOwner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.allOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.anyOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.instanceOf;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static org.glassfish.jersey.client.ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION;


/**
 * @author Robin Müller
 */
public final class BitbucketServerClientService {

    private static transient BitbucketServerClientService instance;
    private final Cache<CacheKey, BitbucketServerAPI> clientCache;

    private BitbucketServerClientService() {
        clientCache = CacheBuilder.<CacheKey, BitbucketServerAPI>newBuilder()
                .maximumSize(50)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    public static BitbucketServerClientService instance() {
        if (instance == null) {
            instance = new BitbucketServerClientService();
        }
        return instance;
    }

    public BitbucketServerAPI getClient(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context) {
        CacheKey key = new CacheKey(clientConfiguration, context);
        BitbucketServerAPI client = clientCache.getIfPresent(key);
        if (client == null) {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(BitbucketServerClientService.class.getClassLoader());
                WebTarget target = ClientBuilder.newBuilder()
                        .register(new BasicAuthFilter(findCredentials(clientConfiguration, context)))
                        .register(new LoggingFilter())
                        .build()
                        .property(SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                        .target(clientConfiguration.getBaseUrl());
                client = createClientProxy(WebResourceFactory.newResource(BitbucketServerAPI.class, target));
                clientCache.put(key, client);
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
        return client;
    }

    public BitbucketPagingClient getPagingClient(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context) {
        return new BitbucketPagingClient(getClient(clientConfiguration, context));
    }

    void put(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context, BitbucketServerAPI client) {
        clientCache.put(new CacheKey(clientConfiguration, context), client);
    }

    private BitbucketServerAPI createClientProxy(BitbucketServerAPI client) {
        return (BitbucketServerAPI) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                                           new Class[]{BitbucketServerAPI.class},
                                                           new ClientProxyInvocationHandler(client));
    }

    private StandardUsernamePasswordCredentials findCredentials(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context) {
        if (StringUtils.isNotBlank(clientConfiguration.getCredentialsId()) && context != null) {
            return firstOrNull(lookupCredentials(StandardUsernamePasswordCredentials.class,
                                                 context,
                                                 context instanceof Queue.Task ? Tasks.getDefaultAuthenticationOf((Queue.Task) context) : ACL.SYSTEM,
                                                 URIRequirementBuilder.fromUri(clientConfiguration.getBaseUrl()).build()),
                               allOf(withId(clientConfiguration.getCredentialsId()), anyOf(instanceOf(StandardUsernamePasswordCredentials.class))));
        }
        return null;
    }

    private static class CacheKey {
        private final BitbucketClientConfiguration clientConfiguration;
        private final SCMSourceOwner context;

        private CacheKey(BitbucketClientConfiguration clientConfiguration, SCMSourceOwner context) {
            this.clientConfiguration = clientConfiguration;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return new EqualsBuilder()
                    .append(clientConfiguration, cacheKey.clientConfiguration)
                    .append(context, cacheKey.context)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(clientConfiguration)
                    .append(context)
                    .toHashCode();
        }
    }

    private static class ClientProxyInvocationHandler implements InvocationHandler {

        private final BitbucketServerAPI client;

        ClientProxyInvocationHandler(BitbucketServerAPI client) {
            this.client = client;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(BitbucketServerClientService.class.getClassLoader());
                return method.invoke(client, args);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause instanceof WebApplicationException || cause instanceof ProcessingException) {
                    throw cause;
                }
                throw e;
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
    }
}
