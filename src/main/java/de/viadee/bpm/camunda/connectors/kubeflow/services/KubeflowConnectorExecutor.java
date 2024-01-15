package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.BasicAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BearerAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;

public class KubeflowConnectorExecutor {
    private static final String KUBEFLOW_URL_ENV = "KF_CONNECTOR_URL";
    private static final String KUBEFLOW_COOKIE_ENV = "KF_CONNECTOR_COOKIE";
    private static final String KUBEFLOW_NAMESPACE_ENV = "KF_CONNECTOR_MULTIUSER_NS";
    private static final String URI_PARAMETER_FILTER = "filter";
    private static final Pair<String, String> URI_PARAMETER_PAIR_V1_TYPE_NS = Pair.of("resource_reference_key.type",
            "NAMESPACE");
    private static final String URI_PARAMETER_V1_ID = "resource_reference_key.id";
    private static final String URI_PARAMETER_V2_NS = "namespace";

    protected long processInstanceKey;
    protected KubeflowConnectorRequest connectorRequest;
    protected KubeflowApisEnum kubeflowApisEnum;
    protected KubeflowApiOperationsEnum kubeflowApiOperationsEnum;
    protected HttpRequest httpRequest;
    protected HttpClient httpClient;
    protected String kubeflowMultiNs;

    private String kubeflowUrl;
    private String kubeflowCookie;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey,
            KubeflowApisEnum kubeflowApisEnum,
            KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.kubeflowApisEnum = kubeflowApisEnum;
        this.kubeflowApiOperationsEnum = kubeflowApiOperationsEnum;

        setConfigurationParameters();

        buildHttpRequest();
    }

    public HttpResponse<String> execute(HttpClient httpClient) {
        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        uriBuilder.setPath(
                String.format(kubeflowApiOperationsEnum.getApiUrl(), kubeflowApisEnum.getUrlPathVersion()));
    }

    protected Map<String, Object> buildPayloadForKubeflowEndpoint() {
        // nop payload by default
        return null;
    }

    protected String getFilterString() {
        return connectorRequest.kubeflowapi().filter();
    }

    private void setConfigurationParameters() {
        var authPropertyGroup = connectorRequest.configuration();

        kubeflowUrl = System.getenv(KUBEFLOW_URL_ENV);
        kubeflowCookie = System.getenv(KUBEFLOW_COOKIE_ENV);
        kubeflowMultiNs = System.getenv(KUBEFLOW_NAMESPACE_ENV);

        if (authPropertyGroup != null) {
            kubeflowUrl = authPropertyGroup.kubeflowUrl() == null ? kubeflowUrl : authPropertyGroup.kubeflowUrl();
            kubeflowCookie = authPropertyGroup.cookievalue() == null ? kubeflowCookie : authPropertyGroup.cookievalue();
            kubeflowMultiNs = authPropertyGroup.multiusernamespace() == null ? kubeflowMultiNs
                    : authPropertyGroup.multiusernamespace();
        }

        if (kubeflowUrl == null || kubeflowCookie == null || kubeflowMultiNs == null) {
            throw new RuntimeException("Configuration parameters not found: url, cookie, and/or namespace null.");
        }
    }

    private void buildHttpRequest() {
        String url = buildUrlForKubeflowEndpoint();

        Map<String, Object> payload = buildPayloadForKubeflowEndpoint();
        BodyPublisher bodyPublisher = ofFormData(payload);
        Builder httpRequestBuilder = HttpRequest.newBuilder()
                .method(kubeflowApiOperationsEnum.getHttpMethod(), bodyPublisher)
                .uri(URI.create(url))
                .setHeader("User-Agent", "Kubeflow Camunda Connector")
                .header("Content-Type", "application/json");

        setAuthentication(httpRequestBuilder);
        setHeaders(httpRequestBuilder);
        httpRequest = httpRequestBuilder.build();
    }

    protected void setHeaders(Builder httpRequestBuilder) {
        httpRequestBuilder.setHeader("Content-Type", "application/json");
    }

    private void setAuthentication(Builder httpRequestBuilder) {
        if (connectorRequest.authentication() instanceof BasicAuthentication) {
            BasicAuthentication basicAuthentication = (BasicAuthentication) connectorRequest.authentication();
            httpRequestBuilder.setHeader("Authorization", getBasicAuthenticationHeader(basicAuthentication.getUsername(), basicAuthentication.getPassword()));
        } else if (connectorRequest.authentication() instanceof BearerAuthentication) {
            BearerAuthentication bearerAuthentication = (BearerAuthentication) connectorRequest.authentication();
            httpRequestBuilder.setHeader("Authorization", "Bearer " + bearerAuthentication.getToken());
        } else {
            // no authentication
        }
    }

    private String buildUrlForKubeflowEndpoint() {
        String url = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(kubeflowUrl);

            addKubeflowUrlPath(uriBuilder);
            addFilter(uriBuilder);
            addNamespaceFilter(uriBuilder);

            url = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    private void addFilter(URIBuilder uriBuilder) throws UnsupportedEncodingException {
        var filter = getFilterString();
        if (filter != null) {
            // this regex removes all new lines and escaping of " before url encoding is
            filter = filter.replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
            uriBuilder.addParameter(URI_PARAMETER_FILTER, URLEncoder.encode(filter, "UTF-8"));
        }
    }

    protected void addNamespaceFilter(URIBuilder uriBuilder) {
        if (kubeflowApiOperationsEnum.requiresMultiuserFilter()) {
            if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
                uriBuilder.addParameter(URI_PARAMETER_PAIR_V1_TYPE_NS.getKey(),
                        URI_PARAMETER_PAIR_V1_TYPE_NS.getValue());
                uriBuilder.addParameter(URI_PARAMETER_V1_ID, kubeflowMultiNs);
            } else {
                uriBuilder.addParameter(URI_PARAMETER_V2_NS, kubeflowMultiNs);
            }
        }
    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    public static HttpRequest.BodyPublisher ofFormData(Map<String, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            var value = entry.getValue();
            builder.append(URLEncoder.encode(value == null ? "" : value.toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
