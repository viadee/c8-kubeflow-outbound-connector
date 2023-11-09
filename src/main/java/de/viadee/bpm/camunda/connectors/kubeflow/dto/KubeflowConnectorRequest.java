package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
        @Valid @NotNull Authentication authentication,
        @Valid @NotNull KubeflowApi kubeflowapi) {

    public HttpCommonRequest getHttpRequest() {

        HttpCommonRequest httpRequest = buildHttpRequest();

        // TODO replace with actual authentication when implemented
        httpRequest.setHeaders(Map.of("Cookie", "authservice_session=" + authentication().cookievalue()));

        return httpRequest;
    }

    private HttpCommonRequest buildHttpRequest() {
        HttpCommonRequest httpRequest = new HttpCommonRequest();
        httpRequest.setUrl(buildKubeflowUrl());
        httpRequest.setMethod(KubeflowApiOperationsEnum.fromValue(kubeflowapi().operation()).getHttpMethod());
        return httpRequest;
    }

    private String buildKubeflowUrl() {
        String url = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(authentication().kubeflowUrl());
            uriBuilder.setPath(KubeflowApiOperationsEnum.fromValue(kubeflowapi().operation()).getApiUrl());
            addMultisiteFilter(uriBuilder);
            url = uriBuilder.build().toString();
            url += buildFilter();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        
        return url;
    }

    private String buildFilter() {
        // remove new lines and escaping of " before url encoding
        String filter = kubeflowapi().filter().replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
        String encodedFilter = "";
        try {
            encodedFilter = "&filter=" + URLEncoder.encode(filter, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return encodedFilter;
    }

    private void addMultisiteFilter(URIBuilder uriBuilder) {
        if(!authentication().multiusernamespace().equals("")) {
            uriBuilder.addParameter("resource_reference_key.type", "NAMESPACE");
            uriBuilder.addParameter("resource_reference_key.id", authentication().multiusernamespace());
        }
    }
}
