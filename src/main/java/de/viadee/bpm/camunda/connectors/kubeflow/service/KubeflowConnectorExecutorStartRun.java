package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.PipelineRunStatusV1;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.PipelineRunStatusV2;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.KubeflowCallable;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutorStartRun extends KubeflowConnectorExecutor {

    private static final List RUN_STATUS_LIST_V1 = List.of(
        PipelineRunStatusV1.SUCCEEDED.getValue(),
        PipelineRunStatusV1.FAILED.getValue(),
        PipelineRunStatusV1.SKIPPED.getValue(),
        PipelineRunStatusV1.ERROR.getValue()
    );

    private static final List RUN_STATUS_LIST_V2 = List.of(
        PipelineRunStatusV2.SUCCEEDED.name(),
        PipelineRunStatusV2.FAILED.name(),
        PipelineRunStatusV2.SKIPPED.name(),
        PipelineRunStatusV2.CANCELED.name()
    );

    private HttpService httpService;

    public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    @Override
    protected Object buildPayloadForKubeflowEndpoint() {
        var pipelineSpec = Map.of("pipeline_id", connectorRequest.kubeflowapi().pipelineId());

        // use process instance key as name of run to be created
        if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
            return getPayloadForEndpointV1(pipelineSpec);
        }
        return getPayloadForEndpointV2(pipelineSpec);
    }

    @Override
    public HttpCommonResult execute(HttpService httpService)
            throws InstantiationException, IllegalAccessException, IOException {
        this.httpService = httpService;
        
        HttpCommonResult result = new HttpCommonResult();

        if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN)) {
            result = startRun();
        } else if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN_AND_MONITOR)) {
            final String idOfAlreadyStartedRun = getIdOfAlreadyStartedRunByName(httpService, Long.toString(processInstanceKey));
            final Duration pollingInterval = Duration.parse(connectorRequest.kubeflowapi().pollingInterval());
            var statusOfRun = "";

            if (idOfAlreadyStartedRun == null) { // run not yet started
                result = startRun();
                String newRunId = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
                    ExecutionHandler.getFieldFromCreateRunResponseV1(result, "id") :
                    ExecutionHandler.getFieldFromCreateRunResponseV2(result, "run_id");
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

    private Map<String, Object> getPayloadForEndpointV1(Map<String, String> pipelineSpec) {
        var resource_reference_key = Map.of("type", "EXPERIMENT", "id",
            connectorRequest.kubeflowapi().experimentId());
        var resource_references = Map.of("key", resource_reference_key);

        var payload = Map.of("name", Long.toString(processInstanceKey), "pipeline_spec",
            pipelineSpec, "resource_references", Arrays.asList(resource_references));

        return payload;
    }

    private Map<String, Object> getPayloadForEndpointV2(Map<String, String> pipelineSpec) {
        return Map.of(
            "display_name", Long.toString(processInstanceKey),
            "pipeline_version_reference", pipelineSpec,
            "experiment_id", connectorRequest.kubeflowapi().experimentId()
        );
    }

    private HttpCommonResult startRun() throws InstantiationException, IllegalAccessException, IOException {
        return httpService.executeConnectorRequest(httpRequest);
    }

    private String getIdOfAlreadyStartedRunByName(HttpService httpService, String runName)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(kubeflowApisEnum.getValue(), KubeflowApiOperationsEnum.GET_RUN_BY_NAME.getValue(), null,
                null, null, runName, null, null, null);
        KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(connectorRequest.authentication(),
                connectorRequest.configuration(), kubeflowApi);
        KubeflowConnectorExecutor getRunByNameExecutor = ExecutionHandler.getExecutor(
                getRunByNameConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByNameExecutor.execute(httpService);

        String id = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
            ExecutionHandler.getFieldFromGetRunByNameResponse(result, "id") :
            ExecutionHandler.getFieldFromGetRunByNameResponse(result, "run_id");
        return id;
    }

    private String retrieveRunStatusWithDelay(KubeflowCallable kubeflowCallable, long delay, boolean isPerformPreCheck) {
        var status = "";
        var statusListToCheckAgainst = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
            RUN_STATUS_LIST_V1 : RUN_STATUS_LIST_V2;
        while (!statusListToCheckAgainst.contains(status)) {
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
