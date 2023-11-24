package de.viadee.bpm.camunda.connectors.kubeflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutor {
    private static final String KUBEFLOW_URL_ENV = "KF_CONNECTOR_URL";
    private static final String KUBEFLOW_COOKIE_ENV = "KF_CONNECTOR_COOKIE";
    private static final String KUBEFLOW_NAMESPACE_ENV = "KF_CONNECTOR_MULTIUSER_NS";
    private static final Pair<String, String> URI_PARAMETER_NAMESPACE_TYPE_PAIR = Pair.of("resource_reference_key.type", "NAMESPACE");
    private static final String URI_PARAMETER_NAMESPACE_ID_KEY = "resource_reference_key.id";

    protected long processInstanceKey;
    protected KubeflowConnectorRequest connectorRequest;
    protected KubeflowApiOperationsEnum kubeflowApiOperationsEnum;
    protected HttpCommonRequest httpRequest;
    protected ObjectMapper objectMapper;

    private String kubeflowUrl;
    private String kubeflowCookie;
    private String kubeflowMultiNs;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.kubeflowApiOperationsEnum = kubeflowApiOperationsEnum;

        objectMapper = new ObjectMapper();

        setAuthenticationParameters();

        buildHttpRequest();
    }

    public HttpCommonResult execute(HttpService httpService)
        throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(httpRequest);
    }

    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        uriBuilder.setPath(kubeflowApiOperationsEnum.getApiUrl());
    }

    protected Object buildPayloadForKubeflowEndpoint() {
        // nop payload by default
        return null;
    }

    protected String getFilterString() {
        return connectorRequest.kubeflowapi().filter();
    }

    private void setAuthenticationParameters() {
        var authPropertyGroup = connectorRequest.authentication();

        kubeflowUrl = System.getenv(KUBEFLOW_URL_ENV);
        kubeflowCookie = System.getenv(KUBEFLOW_COOKIE_ENV);
        kubeflowMultiNs = System.getenv(KUBEFLOW_NAMESPACE_ENV);

        if (authPropertyGroup != null) {
            kubeflowUrl = authPropertyGroup.kubeflowUrl() == null ?
                kubeflowUrl : authPropertyGroup.kubeflowUrl();
            kubeflowCookie = authPropertyGroup.cookievalue() == null ?
                kubeflowCookie : authPropertyGroup.cookievalue();
            kubeflowMultiNs = authPropertyGroup.multiusernamespace() == null ?
                kubeflowMultiNs : authPropertyGroup.multiusernamespace();
        }

        if (kubeflowUrl == null || kubeflowCookie == null || kubeflowMultiNs == null) {
            throw new RuntimeException("Authentication parameters not found: url, cookie, and/or namespace null.");
        }
    }

    private void buildHttpRequest() {
        String url = buildUrlForKubeflowEndpoint();
        Object payload = buildPayloadForKubeflowEndpoint();

        httpRequest = new HttpCommonRequest();
        httpRequest.setUrl(url);
        httpRequest.setMethod(kubeflowApiOperationsEnum.getHttpMethod());
        if (payload != null) {
            httpRequest.setBody(payload);
        }

        // TODO replace with actual authentication when implemented
        httpRequest
            .setHeaders(Map.of("Cookie", "authservice_session=" + kubeflowCookie));
    }

    private String buildUrlForKubeflowEndpoint() {
        String url = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(kubeflowUrl);

            addKubeflowUrlPath(uriBuilder);
            addFilter(uriBuilder);
            addMultisiteFilter(uriBuilder);

            url = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    private void addFilter(URIBuilder uriBuilder) throws UnsupportedEncodingException {
        String filter = "";
        if (getFilterString() != null) {
            // remove new lines and escaping of " before url encoding
            // TODO what does this regex do?
            filter = getFilterString().replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
        }

        if (!filter.equals("")) {
            uriBuilder.addParameter("filter", URLEncoder.encode(filter, "UTF-8"));
        }
    }

    private void addMultisiteFilter(URIBuilder uriBuilder) {
        if (kubeflowApiOperationsEnum.requiresMultiuserFilter() && !kubeflowMultiNs.equals("")) {
            uriBuilder.addParameter(URI_PARAMETER_NAMESPACE_TYPE_PAIR.getKey(), URI_PARAMETER_NAMESPACE_TYPE_PAIR.getValue());
            uriBuilder.addParameter(URI_PARAMETER_NAMESPACE_ID_KEY, kubeflowMultiNs);
        }
    }
}
