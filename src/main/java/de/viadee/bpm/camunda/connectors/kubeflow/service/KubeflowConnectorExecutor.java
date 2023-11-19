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
        kubeflowUrl = System.getenv("KF_CONNECTOR_URL");
        if (connectorRequest.authentication() != null) {
            kubeflowUrl = connectorRequest.authentication().kubeflowUrl();
        }

        kubeflowCookie = System.getenv("KF_CONNECTOR_COOKIE");
        if (connectorRequest.authentication() != null) {
            kubeflowCookie = connectorRequest.authentication().cookievalue();
        }

        kubeflowMultiNs = System.getenv("KF_CONNECTOR_MULTIUSER_NS");
        if (connectorRequest.authentication() != null) {
            kubeflowMultiNs = connectorRequest.authentication().multiusernamespace();
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
