package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.utils.URIBuilder;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutor {
    
    protected long processInstanceKey;
    protected KubeflowConnectorRequest connectorRequest;

    protected HttpCommonRequest httpRequest;

    public KubeflowConnectorExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;

        buildHttpRequest();
    }

    public HttpCommonRequest getHttpRequest() {
        return httpRequest;
    }

    private void buildHttpRequest() {
        httpRequest = new HttpCommonRequest();
        httpRequest.setUrl(buildKubeflowUrl());
        httpRequest.setMethod(KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).getHttpMethod());
        if(buildPayload() != null) {
            httpRequest.setBody(buildPayload());
        }

        // TODO replace with actual authentication when implemented
        httpRequest.setHeaders(Map.of("Cookie", "authservice_session=" + connectorRequest.authentication().cookievalue()));
    }

    protected String buildKubeflowUrlPath() {
        return KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).getApiUrl();
    }

    private String buildKubeflowUrl() {
        String url = "";
        try {
            URIBuilder uriBuilder = new URIBuilder(connectorRequest.authentication().kubeflowUrl());
            uriBuilder.setPath(buildKubeflowUrlPath());
            if(!buildFilter().equals("")) {
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
        if(getFilterString() != null) {
            // remove new lines and escaping of " before url encoding
            filter = getFilterString().replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
        }
        return filter;
    }

    private void addMultisiteFilter(URIBuilder uriBuilder) {
        if(KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).requiresMultiuserFilter() && !connectorRequest.authentication().multiusernamespace().equals("")) {
            uriBuilder.addParameter("resource_reference_key.type", "NAMESPACE");
            uriBuilder.addParameter("resource_reference_key.id", connectorRequest.authentication().multiusernamespace());
        }
    }

    public HttpCommonResult execute(HttpService httpService) throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(getExecutor(connectorRequest, processInstanceKey).getHttpRequest());
    }

    protected <T> T runCallableAfterDelay(Callable<T> task, long delay, TimeUnit timeUnit) throws InterruptedException, ExecutionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            // Schedule the callable task to run after the specified delay
            ScheduledFuture<T> future = scheduler.schedule(task, delay, timeUnit);

            // Wait for the callable to finish and retrieve the result
            T result = future.get(); // This blocks until the result is available

            // Use the result
            return result;
        } finally {
            scheduler.shutdown(); // It's important to shut down the executor service to avoid resource leaks
        }
    }

    public static KubeflowConnectorExecutor getExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        switch (connectorRequest.kubeflowapi().operation()) {
            case "get_run_by_id":
                return new KubeflowConnectorExecutorGetRunById(connectorRequest, processInstanceKey);
            case "get_run_by_name":
                return new KubeflowConnectorExecutorGetRunByName(connectorRequest, processInstanceKey);
            case "start_run":
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey);
            default:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey);
        }
    }
    
}
