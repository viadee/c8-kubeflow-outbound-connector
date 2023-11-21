package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
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

    public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        super(connectorRequest, processInstanceKey);
    }

    @Override
    protected Object buildPayload() {
        final Map<String, String> pipeline_spec = Map.of("pipeline_id", connectorRequest.kubeflowapi().pipelineid());

        final Map<String, Object> resource_reference_key = Map.of("type", "EXPERIMENT", "id",
                connectorRequest.kubeflowapi().experimentid());
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

        if (KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).equals(
                KubeflowApiOperationsEnum.START_RUN)) {
            result = startRun();
        } else if (KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).equals(
                KubeflowApiOperationsEnum.START_RUN_AND_MONITOR)) {
            String status = "";

            // is run already started?
            final String alreadyRunningId = getIdOfAlreadyStartedRun(httpService, Long.toString(processInstanceKey));
            KubeflowCallable kubeflowCallable = new KubeflowCallable(connectorRequest, processInstanceKey, httpService,
                    alreadyRunningId);

            if (alreadyRunningId != null) {
                try {
                    status = ExecutionHandler.runCallableImmediately(kubeflowCallable);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                if (status.equals("Succeeded")) {
                    result.setBody(status);
                    result.setStatus(200);
                } else {
                    // keep checking
                    while (!Arrays.asList(
                            KubeflowConnectorResponse.RUN_STATUS_SUCCEEDED,
                            KubeflowConnectorResponse.RUN_STATUS_ERROR,
                            KubeflowConnectorResponse.RUN_STATUS_FAILED,
                            KubeflowConnectorResponse.RUN_STATUS_SKIPPED).contains(status)) {
                        try {
                            status = ExecutionHandler.runCallableAfterDelay(kubeflowCallable, 5, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            } else {
                // start run
                result = startRun();

                String newRunId = ExecutionHandler.getFieldFromGetRunResponse(result, "id");
                while (!Arrays.asList(
                        KubeflowConnectorResponse.RUN_STATUS_SUCCEEDED,
                        KubeflowConnectorResponse.RUN_STATUS_ERROR,
                        KubeflowConnectorResponse.RUN_STATUS_FAILED,
                        KubeflowConnectorResponse.RUN_STATUS_SKIPPED).contains(status)) {
                    try {
                        status = ExecutionHandler.runCallableAfterDelay(
                                new KubeflowCallable(connectorRequest, processInstanceKey, httpService, newRunId), 5,
                                TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            result.setBody(status);
            result.setStatus(200);
        } else {
            throw new RuntimeException("Unknown kubeflow operation: " + connectorRequest.kubeflowapi().operation());
        }
        return result;
    }

    private HttpCommonResult startRun() throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(
                ExecutionHandler.getExecutor(connectorRequest, processInstanceKey).getHttpRequest());
    }

    private String getIdOfAlreadyStartedRun(HttpService httpService, String runName)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(KubeflowApiOperationsEnum.GET_RUN_BY_NAME.getValue(), null, runName,
                null, null, null);
        KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.authentication(), kubeflowApi);
        KubeflowConnectorExecutor getRunByNameExecutor = ExecutionHandler.getExecutor(
                getRunByNameConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByNameExecutor.execute(httpService);

        String id = (String) ExecutionHandler.getFieldFromGetRunsResponse(result, "id");
        return id;
    }

}
