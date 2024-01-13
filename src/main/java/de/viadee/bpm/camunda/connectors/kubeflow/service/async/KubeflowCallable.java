package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunById;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Run;
import io.swagger.client.model.V2beta1RuntimeState;
import java.io.IOException;
import java.util.concurrent.Callable;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowCallable implements Callable<String> {
    private final String runId;
    private final HttpService httpService;
    private final KubeflowConnectorRequest connectorRequest;
    private final long processInstanceKey;

    public KubeflowCallable(KubeflowConnectorRequest connectorRequest, long processInstanceKey, HttpService httpService,
            String runId) {
        this.connectorRequest = connectorRequest;
        this.processInstanceKey = processInstanceKey;
        this.httpService = httpService;
        this.runId = runId;
    }

    public String call() {
        try {
            return getStatusOfRunById(httpService, runId);
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStatusOfRunById(HttpService httpService, String runId)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(connectorRequest.kubeflowapi().api(), KubeflowApiOperationsEnum.GET_RUN_BY_ID.getValue(),
            runId, null, null, null, null, null, null);
        KubeflowConnectorRequest getRunByIdConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.configuration(), kubeflowApi);
        KubeflowConnectorExecutorGetRunById getRunByIdExecutor = (KubeflowConnectorExecutorGetRunById) ExecutionHandler.getExecutor(
                getRunByIdConnectorRequest,
                processInstanceKey);

        String status = KubeflowApisEnum.PIPELINES_V1.equals(KubeflowApisEnum.fromValue(kubeflowApi.api())) ?
            getRunByIdExecutor.getRunByIdV1Typed(httpService).getStatus() :
            getRunByIdExecutor.getRunByIdV2Typed(httpService).getState().getValue();

        return status;
    }
}
