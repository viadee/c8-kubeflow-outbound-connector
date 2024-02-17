package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BasicAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BearerAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.Constants;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.EnvironmentAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationClientCredentialsFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationPasswordFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.JsonHelper;

public class KubeflowConnectorExecutor {

    private static final String KUBEFLOW_URL_ENV = "KF_CONNECTOR_URL";
    private static final String KUBEFLOW_NAMESPACE_ENV = "KF_CONNECTOR_MULTIUSER_NS";
    private static final String URI_PARAMETER_FILTER = "filter";
    private static final Pair<String, String> URI_PARAMETER_PAIR_V1_TYPE_NS = Pair.of("resource_reference_key.type",
            "NAMESPACE");
    private static final String URI_PARAMETER_V1_ID = "resource_reference_key.id";
    private static final String URI_PARAMETER_V2_NS = "namespace";

    private static final String KUBEFLOW_AUTH_MODE = "KF_AUTH_MODE";
    private static final String KUBEFLOW_AUTH_MODE_NONE = "none";
    private static final String KUBEFLOW_AUTH_MODE_BASIC = "basic";
    private static final String KUBEFLOW_AUTH_MODE_BEARER = "bearer";
    private static final String KUBEFLOW_AUTH_MODE_OAUTH_CC = "oauth_cc";
    private static final String KUBEFLOW_AUTH_MODE_OAUTH_PASSWORD = "oauth_password";
    private static final String KUBEFLOW_AUTH_BASIC_USERNAME = "KF_AUTH_BASIC_USERNAME";
    private static final String KUBEFLOW_AUTH_BASIC_PASSWORD = "KF_AUTH_BASIC_PASSWORD";
    private static final String KUBEFLOW_AUTH_BEARER_TOKEN = "KF_AUTH_BEARER_TOKEN";
    private static final String KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT = "KF_AUTH_OAUTH_TOKEN_ENDPOINT";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_ID = "KF_AUTH_OAUTH_CLIENT_ID";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET = "KF_AUTH_OAUTH_CLIENT_SECRET";
    private static final String KUBEFLOW_AUTH_OAUTH_SCOPES = "KF_AUTH_OAUTH_SCOPES";
    private static final String KUBEFLOW_AUTH_OAUTH_AUDIENCE = "KF_AUTH_OAUTH_AUDIENCE";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH = "KF_AUTH_OAUTH_CLIENT_AUTH";
    private static final String KUBEFLOW_AUTH_OAUTH_USERNAME = "KF_AUTH_OAUTH_USERNAME";
    private static final String KUBEFLOW_AUTH_OAUTH_PASSWORD = "KF_AUTH_OAUTH_PASSWORD";

    protected long processInstanceKey;
    protected KubeflowConnectorRequest connectorRequest;
    protected KubeflowApisEnum kubeflowApisEnum;
    protected KubeflowApiOperationsEnum kubeflowApiOperationsEnum;
    protected HttpRequest httpRequest;
    protected HttpClient httpClient;
    protected String kubeflowMultiNs;
    protected String kubeflowUrl;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey,
            KubeflowApisEnum kubeflowApisEnum,
            KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.kubeflowApisEnum = kubeflowApisEnum;
        this.kubeflowApiOperationsEnum = kubeflowApiOperationsEnum;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectorRequest.getConnectionTimeoutInSeconds())).build();

        setAuthenticationParameters();
        setConfigurationParameters();

        buildHttpRequest();
    }

    public HttpResponse<String> execute() {
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error while running request against kubeflow server: " + httpRequest.uri()
                        + " - " + response.body());
            }
            return response;
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

    protected BodyPublisher buildPayloadForKubeflowEndpoint() {
        // no payload by default
        return BodyPublishers.noBody();
    }

    protected String getFilterString() {
        return connectorRequest.getKubeflowapi().filter();
    }

    private void setAuthenticationParameters() {
        Authentication authPropertyGroup = connectorRequest.getAuthentication();

        if (authPropertyGroup instanceof EnvironmentAuthentication) {
            String authMode = System.getenv(KUBEFLOW_AUTH_MODE);
            if (authMode.equals(KUBEFLOW_AUTH_MODE_NONE)) {
                NoAuthentication noAuthentication = new NoAuthentication();
                connectorRequest.setAuthentication(noAuthentication);
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_BASIC)) {
                BasicAuthentication basicAuthentication = (BasicAuthentication) authPropertyGroup;
                basicAuthentication.setUsername(System.getenv(KUBEFLOW_AUTH_BASIC_USERNAME));
                basicAuthentication.setPassword(System.getenv(KUBEFLOW_AUTH_BASIC_PASSWORD));
                connectorRequest.setAuthentication(basicAuthentication);

                if (StringUtils.isBlank(basicAuthentication.getUsername()) || StringUtils.isBlank(basicAuthentication.getPassword())) {
                    throw new RuntimeException("Authentication parameters missing for basic authentication in environment! Required are username and password.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_BEARER)) {
                BearerAuthentication bearerAuthentication = (BearerAuthentication) authPropertyGroup;
                bearerAuthentication.setToken(System.getenv(KUBEFLOW_AUTH_BEARER_TOKEN));
                connectorRequest.setAuthentication(bearerAuthentication);
                if (StringUtils.isBlank(bearerAuthentication.getToken())) {
                    throw new RuntimeException("Authentication parameters missing for bearer authentication in environment! Required is token.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_OAUTH_CC)) {
                OAuthAuthenticationClientCredentialsFlow oAuthAuthenticationClientCredentialsFlow = new OAuthAuthenticationClientCredentialsFlow();
                oAuthAuthenticationClientCredentialsFlow.setOauthTokenEndpoint(System.getenv(KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT));
                oAuthAuthenticationClientCredentialsFlow.setClientId(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_ID));
                oAuthAuthenticationClientCredentialsFlow.setClientSecretCC(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET));
                oAuthAuthenticationClientCredentialsFlow.setScopes(System.getenv(KUBEFLOW_AUTH_OAUTH_SCOPES));
                oAuthAuthenticationClientCredentialsFlow.setAudience(System.getenv(KUBEFLOW_AUTH_OAUTH_AUDIENCE));
                oAuthAuthenticationClientCredentialsFlow.setClientAuthentication(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH));
                connectorRequest.setAuthentication(oAuthAuthenticationClientCredentialsFlow);
                if (StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getOauthTokenEndpoint()) 
                    || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientId())
                    || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientSecretCC())
                    || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getScopes())
                    || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientAuthentication())
                    ) {
                    throw new RuntimeException("Authentication parameters missing for OAuth (client-credentials flow) authentication in environment! Required are token endpoint, clientId, clientSecret, scopes and client authentication.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_OAUTH_PASSWORD)) {
                OAuthAuthenticationPasswordFlow oAuthAuthenticationPasswordFlow = (OAuthAuthenticationPasswordFlow) authPropertyGroup;
                oAuthAuthenticationPasswordFlow.setOauthTokenEndpoint(System.getenv(KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT));
                oAuthAuthenticationPasswordFlow.setClientId(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_ID));
                oAuthAuthenticationPasswordFlow.setClientSecretPW(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET));
                oAuthAuthenticationPasswordFlow.setScopes(System.getenv(KUBEFLOW_AUTH_OAUTH_SCOPES));
                oAuthAuthenticationPasswordFlow.setAudience(System.getenv(KUBEFLOW_AUTH_OAUTH_AUDIENCE));
                oAuthAuthenticationPasswordFlow.setClientAuthentication(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH));
                oAuthAuthenticationPasswordFlow.setUsername(System.getenv(KUBEFLOW_AUTH_OAUTH_USERNAME));
                oAuthAuthenticationPasswordFlow.setPassword(System.getenv(KUBEFLOW_AUTH_OAUTH_PASSWORD));
                connectorRequest.setAuthentication(oAuthAuthenticationPasswordFlow);
                if (StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getOauthTokenEndpoint()) 
                    || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getClientId())
                    || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getScopes())
                    || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getClientAuthentication())
                    || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getUsername())
                    || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getPassword())
                    ) {
                    throw new RuntimeException("Authentication parameters missing for OAuth (password flow) authentication in environment! Required are token endpoint, clientId, scopes, client authentication, username and password.");
                }
            }
        }
    }

    private void setConfigurationParameters() {
        var configPropertyGroup = connectorRequest.getConfiguration();

        kubeflowUrl = System.getenv(KUBEFLOW_URL_ENV);
        kubeflowMultiNs = System.getenv(KUBEFLOW_NAMESPACE_ENV);

        if (configPropertyGroup != null) {
            kubeflowUrl = StringUtils.isBlank(configPropertyGroup.kubeflowUrl()) ? kubeflowUrl : configPropertyGroup.kubeflowUrl();
            kubeflowMultiNs = StringUtils.isBlank(configPropertyGroup.multiusernamespace()) ? kubeflowMultiNs
                    : configPropertyGroup.multiusernamespace();
        }

        if (kubeflowUrl == null || kubeflowMultiNs == null) {
            throw new RuntimeException("Configuration parameters not found: url, cookie, and/or namespace null.");
        }
    }

    private void buildHttpRequest() {
        String url = buildUrlForKubeflowEndpoint();

        BodyPublisher bodyPublisher = buildPayloadForKubeflowEndpoint();
        Builder httpRequestBuilder = HttpRequest.newBuilder()
                .method(kubeflowApiOperationsEnum.getHttpMethod(), bodyPublisher)
                .uri(URI.create(url))
                .setHeader("User-Agent", "Kubeflow Camunda Connector");

        setAuthentication(httpRequestBuilder);
        setHeaders(httpRequestBuilder);
        httpRequest = httpRequestBuilder.build();
    }

    protected void setHeaders(Builder httpRequestBuilder) {
        Map<String, String> httpHeadersFromPropertiesPanel = this.connectorRequest.getKubeflowapi().httpHeaders();
        if(httpHeadersFromPropertiesPanel != null && httpHeadersFromPropertiesPanel.keySet().size() > 0) {
            httpHeadersFromPropertiesPanel.keySet().forEach(key -> httpRequestBuilder
                .setHeader(key, httpHeadersFromPropertiesPanel.get(key)));
        }
        httpRequestBuilder.setHeader("Content-Type", "application/json");
    }

    private void setAuthentication(Builder httpRequestBuilder) {
        if (connectorRequest.getAuthentication() instanceof BasicAuthentication) {
            BasicAuthentication basicAuthentication = (BasicAuthentication) connectorRequest.getAuthentication();
            httpRequestBuilder.setHeader("Authorization",
                    getBasicAuthenticationHeader(basicAuthentication.getUsername(), basicAuthentication.getPassword()));
        } else if (connectorRequest.getAuthentication() instanceof BearerAuthentication) {
            BearerAuthentication bearerAuthentication = (BearerAuthentication) connectorRequest.getAuthentication();
            httpRequestBuilder.setHeader("Authorization", "Bearer " + bearerAuthentication.getToken());
        } else if (connectorRequest.getAuthentication() instanceof OAuthAuthenticationClientCredentialsFlow) {
            OAuthAuthenticationClientCredentialsFlow oAuthAuthentication = (OAuthAuthenticationClientCredentialsFlow) connectorRequest
                    .getAuthentication();
            String accessToken = getAccessTokenFromClientCredentialsFlow(oAuthAuthentication);
            httpRequestBuilder.setHeader("Authorization", "Bearer " + accessToken);
        } else if (connectorRequest.getAuthentication() instanceof OAuthAuthenticationPasswordFlow) {
            OAuthAuthenticationPasswordFlow oAuthAuthenticationPasswordFlow = (OAuthAuthenticationPasswordFlow) connectorRequest
                    .getAuthentication();
            String accessToken = getAccessTokenFromPasswordFlow(oAuthAuthenticationPasswordFlow);
            httpRequestBuilder.setHeader("Authorization", "Bearer " + accessToken);
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
            if (entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private String getAccessTokenFromClientCredentialsFlow(OAuthAuthenticationClientCredentialsFlow authentication) {
        String serviceUrl = authentication.getOauthTokenEndpoint();
        Map<String, Object> data = new HashMap<>();
        data.put("grant_type", authentication.getGrantType());
        data.put("client_id", authentication.getClientId());
        data.put("client_secret", authentication.getClientSecretCC());
        data.put("scope", authentication.getScopes());

        return requestAccessToken(serviceUrl, data);
    }

    private String getAccessTokenFromPasswordFlow(OAuthAuthenticationPasswordFlow authentication) {

        String serviceUrl = authentication.getOauthTokenEndpoint();
        Map<String, Object> data = new HashMap<>();
        data.put("username", authentication.getUsername());
        data.put("password", authentication.getPassword());
        data.put("grant_type", authentication.getGrantType());
        data.put("client_id", authentication.getClientId());
        data.put("client_secret", authentication.getClientSecretPW());
        data.put("scope", authentication.getScopes());

        return requestAccessToken(serviceUrl, data);
    }

    private String requestAccessToken(String serviceUrl, Map<String, Object> data) {
        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", ofFormData(data))
                .uri(URI.create(serviceUrl))
                .setHeader("User-Agent", "Kubeflow Camunda Connector")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException(response.body());
            }

            String accessToken = extractOAuthAccessToken(response);
            return accessToken;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractOAuthAccessToken(HttpResponse<String> oauthResponse) throws IOException {
        return Optional.ofNullable(JsonHelper.getAsJsonElement(oauthResponse.body(), JsonHelper.objectMapper))
                .map(jsonNode -> jsonNode.findValue(Constants.ACCESS_TOKEN).asText())
                .orElse(null);
    }
}
