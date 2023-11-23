package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.ExecutionHandler;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutor {
    private static final String KUBEFLOW_URL_ENV = "KF_CONNECTOR_URL";
    private static final String KUBEFLOW_COOKIE_ENV = "KF_CONNECTOR_COOKIE";
    private static final String KUBEFLOW_NAMESPACE_ENV = "KF_CONNECTOR_MULTIUSER_NS";

    protected long processInstanceKey;
    protected KubeflowConnectorRequest connectorRequest;

    protected HttpCommonRequest httpRequest;
    private String kubeflowUrl;
    private String kubeflowCookie;
    private String kubeflowMultiNs;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;

        setAuthenticationParameters();

        buildHttpRequest();
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

    public HttpCommonRequest getHttpRequest() {
        return httpRequest;
    }

    private void buildHttpRequest() {
        httpRequest = new HttpCommonRequest();
        httpRequest.setUrl(buildKubeflowUrl());
        httpRequest.setMethod(
                KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).getHttpMethod());
        if (buildPayload() != null) {
            httpRequest.setBody(buildPayload());
        }

        // TODO replace with actual authentication when implemented
        httpRequest
                .setHeaders(Map.of("Cookie", "authservice_session=" + kubeflowCookie));
    }

    protected String buildKubeflowUrlPath() {
        return KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).getApiUrl();
    }

    private String buildKubeflowUrl() {
        String url = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(kubeflowUrl);
            uriBuilder.setPath(buildKubeflowUrlPath());
            if (!buildFilter().equals("")) {
                uriBuilder.addParameter("filter", URLEncoder.encode(buildFilter(), "UTF-8"));
            }
            addMultisiteFilter(uriBuilder);
            url = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    protected Object buildPayload() {
        // nop payload by default
        return null;
    }

    protected String getFilterString() {
        return connectorRequest.kubeflowapi().filter();
    }

    private String buildFilter() {
        String filter = "";
        if (getFilterString() != null) {
            // remove new lines and escaping of " before url encoding
            filter = getFilterString().replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
        }
        return filter;
    }

    private void addMultisiteFilter(URIBuilder uriBuilder) {
        if (KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).requiresMultiuserFilter()
                && !kubeflowMultiNs.equals("")) {
            uriBuilder.addParameter("resource_reference_key.type", "NAMESPACE");
            uriBuilder.addParameter("resource_reference_key.id",
                    kubeflowMultiNs);
        }
    }

    public HttpCommonResult execute(HttpService httpService)
            throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(
                ExecutionHandler.getExecutor(connectorRequest, processInstanceKey).getHttpRequest());
    }
}
