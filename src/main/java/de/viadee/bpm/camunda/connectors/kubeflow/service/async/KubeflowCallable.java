package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.Callable;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutor;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowCallable implements Callable<String> {
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

    public String call() {
        try {
            return getStatusOfRunById(httpClient, runId);
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStatusOfRunById(HttpClient httpClient, String runId)
            throws InstantiationException, IllegalAccessException, IOException {
        httpClient.executor()
        KubeflowApi kubeflowApi = new KubeflowApi(connectorRequest.kubeflowapi().api(), KubeflowApiOperationsEnum.GET_RUN_BY_ID.getValue(),
            runId, null, null, null, null, null, null);
        KubeflowConnectorRequest getRunByIdConnectorRequest = new KubeflowConnectorRequest(connectorRequest.authentication(),
                connectorRequest.configuration(), kubeflowApi);
        KubeflowConnectorExecutor getRunByIdExecutor = ExecutionHandler.getExecutor(
                getRunByIdConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByIdExecutor.execute(httpClient);

        String status = KubeflowApisEnum.PIPELINES_V1.equals(KubeflowApisEnum.fromValue(kubeflowApi.api())) ?
            ExecutionHandler.getFieldFromCreateRunResponseV1(result, "status") :
            ExecutionHandler.getFieldFromCreateRunResponseV2(result, "state");

        return status;
    }
}
