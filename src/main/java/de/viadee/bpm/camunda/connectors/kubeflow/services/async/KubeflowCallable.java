package de.viadee.bpm.camunda.connectors.kubeflow.services.async;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorGetRunById;

public class KubeflowCallable implements Callable<HttpResponse<String>> {
    private final String runId;
    private final HttpClient httpClient;
    private final KubeflowConnectorRequest connectorRequest;
    private final long processInstanceKey;

    public KubeflowCallable(KubeflowConnectorRequest connectorRequest, long processInstanceKey, HttpClient httpClient,
            String runId) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.httpClient = httpClient;
        this.runId = runId;
    }

    public HttpResponse<String> call() {
        try {
            return getStatusOfRunById(httpClient, runId);
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> getStatusOfRunById(HttpClient httpClient, String runId)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(connectorRequest.kubeflowapi().api(), KubeflowApiOperationsEnum.GET_RUN_BY_ID.getValue(),
            runId, null, null, null, null, null, null);
        KubeflowConnectorRequest getRunByIdConnectorRequest = new KubeflowConnectorRequest(connectorRequest.authentication(),
                connectorRequest.configuration(), kubeflowApi);
        KubeflowConnectorExecutorGetRunById getRunByIdExecutor = (KubeflowConnectorExecutorGetRunById) ExecutionHandler.getExecutor(
                getRunByIdConnectorRequest,
                processInstanceKey);

        var httpResponse = getRunByIdExecutor.execute(httpClient);
        
        return httpResponse;
    }
}
