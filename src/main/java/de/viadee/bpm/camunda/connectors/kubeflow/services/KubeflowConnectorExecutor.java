package de.viadee.bpm.camunda.connectors.kubeflow.services;

import de.viadee.bpm.camunda.connectors.kubeflow.enums.TypeOfUserModeEnum;
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
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.BasicAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BearerAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationClientCredentialsFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationPasswordFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthParamsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.AuthUtil;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.JsonHelper;

public class KubeflowConnectorExecutor {

    private static final String KUBEFLOW_URL_ENV = "KF_CONNECTOR_URL";
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
    protected boolean isMultiUserMode;
    protected String kubeflowUrl;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey,
            KubeflowApisEnum kubeflowApisEnum,
            KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.kubeflowApisEnum = kubeflowApisEnum;
        this.kubeflowApiOperationsEnum = kubeflowApiOperationsEnum;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectorRequest.getTimeout().connectionTimeoutInSeconds())).build();

        AuthUtil.setAuthenticationParameters(connectorRequest);
        setConfigurationParameters();

        buildHttpRequest();
    }

    public HttpResponse<String> execute() {
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
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

    private void setConfigurationParameters() {
        var configPropertyGroup = connectorRequest.getConfiguration();
        var typeOfUserMode = TypeOfUserModeEnum.fromValue(configPropertyGroup.typeOfUserMode());

        kubeflowUrl = System.getenv(KUBEFLOW_URL_ENV);

        if (configPropertyGroup != null) {
            kubeflowUrl = StringUtils.isBlank(configPropertyGroup.kubeflowUrl()) ? kubeflowUrl : configPropertyGroup.kubeflowUrl();
            isMultiUserMode = TypeOfUserModeEnum.MULTI_USER_MODE.equals(typeOfUserMode);
        }

        if (kubeflowUrl == null) {
            throw new RuntimeException("Configuration parameters not found: kubeflow url is null.");
        }
    }

    private void buildHttpRequest() {
        String url = buildUrlForKubeflowEndpoint();

        BodyPublisher bodyPublisher = buildPayloadForKubeflowEndpoint();
        Builder httpRequestBuilder = HttpRequest.newBuilder()
                .method(kubeflowApiOperationsEnum.getHttpMethod(), bodyPublisher)
                .uri(URI.create(url))
                .setHeader(HttpHeaders.USER_AGENT, "Kubeflow Camunda Connector");

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
        httpRequestBuilder.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
    }

    private void setAuthentication(Builder httpRequestBuilder) {
        if (connectorRequest.getAuthentication() instanceof BasicAuthentication) {
            BasicAuthentication basicAuthentication = (BasicAuthentication) connectorRequest.getAuthentication();
            httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION,
                    getBasicAuthenticationHeader(basicAuthentication.getUsername(), basicAuthentication.getPassword()));
        } else if (connectorRequest.getAuthentication() instanceof BearerAuthentication) {
            BearerAuthentication bearerAuthentication = (BearerAuthentication) connectorRequest.getAuthentication();
            httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuthentication.getToken());
        } else if (connectorRequest.getAuthentication() instanceof OAuthAuthenticationClientCredentialsFlow) {
            OAuthAuthenticationClientCredentialsFlow oAuthAuthentication = (OAuthAuthenticationClientCredentialsFlow) connectorRequest
                    .getAuthentication();
            String accessToken = getAccessTokenFromClientCredentialsFlow(oAuthAuthentication);
            httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        } else if (connectorRequest.getAuthentication() instanceof OAuthAuthenticationPasswordFlow) {
            OAuthAuthenticationPasswordFlow oAuthAuthenticationPasswordFlow = (OAuthAuthenticationPasswordFlow) connectorRequest
                    .getAuthentication();
            String accessToken = getAccessTokenFromPasswordFlow(oAuthAuthenticationPasswordFlow);
            httpRequestBuilder.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
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
            uriBuilder.addParameter(URI_PARAMETER_FILTER, URLEncoder.encode(filter, StandardCharsets.UTF_8.toString()));
        }
    }

    protected void addNamespaceFilter(URIBuilder uriBuilder) {
        if (isMultiUserMode) {
            var namespace = connectorRequest.getKubeflowapi().namespace();
            if (kubeflowApiOperationsEnum.isNamespaceFilterRequired()) {
                if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
                    uriBuilder.addParameter(URI_PARAMETER_PAIR_V1_TYPE_NS.getKey(),
                        URI_PARAMETER_PAIR_V1_TYPE_NS.getValue());
                    uriBuilder.addParameter(URI_PARAMETER_V1_ID, namespace);
                } else {
                    uriBuilder.addParameter(URI_PARAMETER_V2_NS, namespace);
                }
            } else if (KubeflowApiOperationsEnum.GET_PIPELINES.equals(kubeflowApiOperationsEnum) 
                        && namespace != null) {
                if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
                    uriBuilder.addParameter(URI_PARAMETER_PAIR_V1_TYPE_NS.getKey(),
                        URI_PARAMETER_PAIR_V1_TYPE_NS.getValue());
                    uriBuilder.addParameter(URI_PARAMETER_V1_ID, namespace);
                } else {
                    uriBuilder.addParameter(URI_PARAMETER_V2_NS, namespace);
                }
            }
        }
    }

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
        data.put(OAuthParamsEnum.GRANT_TYPE.toString(), authentication.getGrantType());
        data.put(OAuthParamsEnum.CLIENT_ID.toString(), authentication.getClientId());
        data.put(OAuthParamsEnum.CLIENT_SECRET.toString(), authentication.getClientSecretCC());
        data.put(OAuthParamsEnum.SCOPE.toString(), authentication.getScopes());

        return requestAccessToken(serviceUrl, data);
    }

    private String getAccessTokenFromPasswordFlow(OAuthAuthenticationPasswordFlow authentication) {

        String serviceUrl = authentication.getOauthTokenEndpoint();
        Map<String, Object> data = new HashMap<>();
        data.put(OAuthParamsEnum.USERNAME.toString(), authentication.getUsername());
        data.put(OAuthParamsEnum.PASSWORD.toString(), authentication.getPassword());
        data.put(OAuthParamsEnum.GRANT_TYPE.toString(), authentication.getGrantType());
        data.put(OAuthParamsEnum.CLIENT_ID.toString(), authentication.getClientId());
        data.put(OAuthParamsEnum.CLIENT_SECRET.toString(), authentication.getClientSecretPW());
        data.put(OAuthParamsEnum.SCOPE.toString(), authentication.getScopes());

        return requestAccessToken(serviceUrl, data);
    }

    private String requestAccessToken(String serviceUrl, Map<String, Object> data) {
        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", ofFormData(data))
                .uri(URI.create(serviceUrl))
                .setHeader(HttpHeaders.USER_AGENT, "Kubeflow Camunda Connector")
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
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
                .map(jsonNode -> jsonNode.findValue(OAuthParamsEnum.ACCESS_TOKEN.toString()).asText())
                .orElse(null);
    }
}
