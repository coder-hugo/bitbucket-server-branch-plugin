package org.jenkinsci.plugins.bitbucket.server.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.bitbucket.server.api.BitbucketServerAPI;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Robin Müller
 */
@Priority(Priorities.USER)
class LoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final Logger LOGGER = Logger.getLogger(BitbucketServerAPI.class.getName());

    LoggingFilter() {}

    private static String toPrettyPrint(String json) {
        try {
            return OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readValue(json, Object.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void filter(ClientRequestContext context) throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Call Bitbucket Server:\nHTTP method: {0}\nURL: {1}\nRequest headers: [\n{2}\n]",
                       new Object[]{context.getMethod(), context.getUri(), toFilteredString(context.getHeaders())});
        }
    }

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Got response from Bitbucket Server:\nURL: {0}\nStatus: {1} {2}\nResponse headers: [\n{3}\n]\nResponse body: {4}",
                       new Object[]{request.getUri(), response.getStatus(), response.getStatusInfo(), toString(response.getHeaders()),
                               getPrettyPrintResponseBody(response)});
        }
    }

    private String toFilteredString(MultivaluedMap<String, Object> headers) {
        return FluentIterable.from(headers.entrySet()).transform(new HeaderToFilteredString()).join(Joiner.on(",\n"));
    }

    private String toString(MultivaluedMap<String, String> headers) {
        return FluentIterable.from(headers.entrySet()).transform(new HeaderToString()).join(Joiner.on(",\n"));
    }

    private String getPrettyPrintResponseBody(ClientResponseContext responseContext) {
        String responseBody = getResponseBody(responseContext);
        if (StringUtils.isNotEmpty(responseBody) && responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            return toPrettyPrint(responseBody);
        }
        return responseBody;
    }

    private String getResponseBody(ClientResponseContext context) {
        try (InputStream entityStream = context.getEntityStream()) {
            if (entityStream != null) {
                byte[] bytes = IOUtils.toByteArray(entityStream);
                context.setEntityStream(new ByteArrayInputStream(bytes));
                return new String(bytes);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failure during reading the response body", e);
            context.setEntityStream(new ByteArrayInputStream(new byte[0]));
        }
        return "";
    }

    private static class HeaderToFilteredString implements Function<Map.Entry<String, List<Object>>, String> {
        @Nullable
        @Override
        public String apply(@Nullable Map.Entry<String, List<Object>> input) {
            if (input == null) {
                return null;
            } else if (input.getKey().equals("Authorization")) {
                return input.getKey() + " = [****FILTERED****]";
            } else {
                return input.getKey() + " = [" + Joiner.on(", ").join(input.getValue()) + "]";
            }
        }
    }

    private static class HeaderToString implements Function<Map.Entry<String, List<String>>, String> {
        @Nullable
        @Override
        public String apply(@Nullable Map.Entry<String, List<String>> input) {
            return input == null ? null : input.getKey() + " = [" + Joiner.on(", ").join(input.getValue()) + "]";
        }
    }
}
