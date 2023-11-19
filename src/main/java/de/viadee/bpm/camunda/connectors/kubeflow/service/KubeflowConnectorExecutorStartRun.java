package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.CallableRunner;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.KubeflowCallable;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutorStartRun extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        super(connectorRequest, processInstanceKey);
    }

    @Override
    protected Object buildPayload() {
        final Map<String, String> pipeline_spec = Map.of("pipeline_id", "28abf431-7475-442c-8ea6-4dacb252fa02");

        final Map<String, Object> resource_reference_key = Map.of("type", "EXPERIMENT", "id",
                "4ce6a43d-81d3-4a3f-a191-7dd1b04bb203");
        final Map<String, Object> resource_references = Map.of("key", resource_reference_key);

        final Map<String, Object> payload = Map.of("name", Long.toString(processInstanceKey), "pipeline_spec",
                pipeline_spec, "resource_references", Arrays.asList(resource_references));
        return payload;
    }

    @Override
    public HttpCommonResult execute(HttpService httpService)
            throws InstantiationException, IllegalAccessException, IOException {
        HttpCommonResult result = new HttpCommonResult();

        String status = "";

        // is run already started?
        final String alreadyRunningId = getIdOfAlreadyStartedRun(httpService, Long.toString(processInstanceKey));
        KubeflowCallable kubeflowCallable = new KubeflowCallable(connectorRequest, processInstanceKey, httpService,
                alreadyRunningId);

        if (alreadyRunningId != null) {
            try {
                status = CallableRunner.runCallableAfterDelay(kubeflowCallable, 0, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (status.equals("Succeeded")) {
                result.setBody(status);
                result.setStatus(200);
            } else {
                // keep checking
                while (!status.equals("Succeeded") && !status.equals("Failed")) {
                    try {
                        status = CallableRunner.runCallableAfterDelay(kubeflowCallable, 5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            // start run
            result = httpService
                    .executeConnectorRequest(
                            ExecutionHandler.getExecutor(connectorRequest, processInstanceKey).getHttpRequest());
            LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) result.getBody();
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) body.get("run");
            String newRunId = (String) map.get("id");
            while (!status.equals("Succeeded") && !status.equals("Failed")) {
                try {
                    status = CallableRunner.runCallableAfterDelay(
                            new KubeflowCallable(connectorRequest, processInstanceKey, httpService, newRunId), 5,
                            TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        result.setBody(status);
        result.setStatus(200);
        return result;
    }

    private String getIdOfAlreadyStartedRun(HttpService httpService, String runName)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi(KubeflowApiOperationsEnum.GET_RUN_BY_NAME.getValue(), null, runName,
                null);
        KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.authentication(), kubeflowApi);
        KubeflowConnectorExecutor getRunByNameExecutor = ExecutionHandler.getExecutor(
                getRunByNameConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByNameExecutor.execute(httpService);
        if (result.getBody() instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) result.getBody();
            if (body.size() > 0) {
                if (body.get("runs") instanceof ArrayList) {
                    ArrayList<?> runs = (ArrayList<?>) body.get("runs");
                    if (!runs.isEmpty() && runs.get(0) instanceof LinkedHashMap) {
                        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) runs.get(0);
                        String id = (String) map.get("id");
                        return id;
                    }
                }
                // error: throw RuntimeException
                throw new RuntimeException("result auf kubeflow api contained unexpected data");
            } else {
                return null;
            }
        } else {
            // error: throw RuntimeException
            throw new RuntimeException("result auf kubeflow api contained unexpected data");
        }
    }

}
