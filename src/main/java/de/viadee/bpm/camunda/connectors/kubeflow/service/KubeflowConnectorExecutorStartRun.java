package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorResponse;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.KubeflowCallable;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutorStartRun extends KubeflowConnectorExecutor {

    private HttpService httpService;

    public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApiOperationsEnum);
    }

    @Override
    protected Object buildPayloadForKubeflowEndpoint() {
        final Map<String, String> pipeline_spec = Map.of("pipeline_id", connectorRequest.kubeflowapi().pipelineId());

        final Map<String, Object> resource_reference_key = Map.of("type", "EXPERIMENT", "id",
                connectorRequest.kubeflowapi().experimentId());
        final Map<String, Object> resource_references = Map.of("key", resource_reference_key);

        final Map<String, Object> payload = Map.of("name", Long.toString(processInstanceKey), "pipeline_spec",
                pipeline_spec, "resource_references", Arrays.asList(resource_references));
        return payload;
    }

    @Override
    public HttpCommonResult execute(HttpService httpService)
            throws InstantiationException, IllegalAccessException, IOException {
        this.httpService = httpService;
        
        HttpCommonResult result = new HttpCommonResult();

        if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN)) {
            result = startRun();
        } else if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN_AND_MONITOR)) {
            final String idOfAlreadyStartedRun = getIdOfAlreadyStartedRun(httpService, Long.toString(processInstanceKey));
            final Duration pollingInterval = Duration.parse(connectorRequest.kubeflowapi().pollingInterval());
            var statusOfRun = "";

            if (idOfAlreadyStartedRun == null) { // run not yet started
                result = startRun();
                String newRunId = ExecutionHandler.getFieldFromGetRunResponse(result, "id");
                KubeflowCallable kubeflowCallableRunNotStarted = new KubeflowCallable(connectorRequest, processInstanceKey, httpService, newRunId);
                statusOfRun = retrieveRunStatusWithDelay(kubeflowCallableRunNotStarted, pollingInterval.getSeconds(), false);

            } else { // run already started
                KubeflowCallable kubeflowCallableRunStarted = new KubeflowCallable(connectorRequest, processInstanceKey, httpService,
                    idOfAlreadyStartedRun);
                statusOfRun = retrieveRunStatusWithDelay(kubeflowCallableRunStarted, pollingInterval.getSeconds(), true);
            }

            result.setBody(statusOfRun);
            result.setStatus(200);

        } else {
            throw new RuntimeException("Unknown kubeflow operation: " + connectorRequest.kubeflowapi().operation());
        }

        return result;
    }

    private HttpCommonResult startRun() throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(httpRequest);
    }

    private String getIdOfAlreadyStartedRun(HttpService httpService, String runName)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(KubeflowApiOperationsEnum.GET_RUN_BY_NAME.getValue(), null,
                null, null, runName, null);
        KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.authentication(), kubeflowApi);
        KubeflowConnectorExecutor getRunByNameExecutor = ExecutionHandler.getExecutor(
                getRunByNameConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByNameExecutor.execute(httpService);

        String id = (String) ExecutionHandler.getFieldFromGetRunsResponse(result, "id");
        return id;
    }

    private String retrieveRunStatusWithDelay(KubeflowCallable kubeflowCallable, long delay, boolean isPerformPreCheck) {
        var status = "";
        while (!Arrays.asList(
            KubeflowConnectorResponse.RUN_STATUS_SUCCEEDED,
            KubeflowConnectorResponse.RUN_STATUS_ERROR,
            KubeflowConnectorResponse.RUN_STATUS_FAILED,
            KubeflowConnectorResponse.RUN_STATUS_SKIPPED).contains(status)) {
            try {
                if (isPerformPreCheck) {
                    status = ExecutionHandler.runCallableAfterDelay(kubeflowCallable, 0, TimeUnit.SECONDS);
                    isPerformPreCheck = false;
                } else {
                    status = ExecutionHandler.runCallableAfterDelay(kubeflowCallable, delay, TimeUnit.SECONDS);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return status;
    }

}
