package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;

public class KubeflowConnectorExecutorStartRun extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        super(connectorRequest, processInstanceKey);
    }

    @Override
    protected Object buildPayload() {
        final Map<String, String> pipeline_spec = Map.of("pipeline_id", "3be45825-7e46-437f-a19b-230590eb5204");

        final Map<String, Object> resource_reference_key = Map.of("type", "EXPERIMENT", "id",
                "ac61e0b4-9609-40b1-b466-577df3276a21");
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
        String alreadyRunningId = getIdOfAlreadyStartedRun(httpService, Long.toString(processInstanceKey));
        if (alreadyRunningId != null) {
            status = getStatusOfRunById(httpService, alreadyRunningId);
            if (status.equals("Succeeded")) {
                result.setBody(status);
                result.setStatus(200);
            } else {
                // keep checking
                // checkStatusOfRunByIdInSeconds(httpService, alreadyRunningId, 5);
                Callable<String> getStatusOfRunByIdCallable = () -> {
                    return getStatusOfRunById(httpService, alreadyRunningId);
                };
                while (!status.equals("Succeeded") && !status.equals("Failed")) {
                    try {
                        status = runCallableAfterDelay(getStatusOfRunByIdCallable, 5, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            // start run
            httpService
                    .executeConnectorRequest(getExecutor(connectorRequest, processInstanceKey).getHttpRequest());
            Callable<String> getStatusOfRunByIdCallable = () -> {
                return getStatusOfRunById(httpService, alreadyRunningId);
            };
            while (!status.equals("Succeeded") && !status.equals("Failed")) {
                try {
                    status = runCallableAfterDelay(getStatusOfRunByIdCallable, 5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        result.setBody(status);
        result.setStatus(200);
        return result;

        // try {
        // // Schedule the callable to run after 10 minutes and retrieve the result
        // result = runCallableAfterDelay(getRunByNameCallable, 5, TimeUnit.SECONDS);

        // LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>)
        // result.getBody();
        // if (body.size() > 0) {
        // LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)
        // ((ArrayList<LinkedHashMap<String, Object>>) body
        // .get("runs")).get(0);
        // String id = (String) map.get("id");
        // }

        // // Use the result
        // return result;
        // } catch (InterruptedException | ExecutionException e) {
        // throw new RuntimeException(e);
        // }
    }

    private String getIdOfAlreadyStartedRun(HttpService httpService, String runName)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi("get_run_by_name", null, runName, null);
        KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.authentication(), kubeflowApi);
        KubeflowConnectorExecutor getRunByNameExecutor = KubeflowConnectorExecutor.getExecutor(
                getRunByNameConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByNameExecutor.execute(httpService);
        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) result.getBody();
        if (body.size() > 0) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) ((ArrayList<LinkedHashMap<String, Object>>) body
                    .get("runs")).get(0);
            String id = (String) map.get("id");
            return id;
        } else {
            return null;
        }
    }

    private String getStatusOfRunById(HttpService httpService, String runId)
            throws InstantiationException, IllegalAccessException, IOException {
        KubeflowApi kubeflowApi = new KubeflowApi("get_run_by_id", runId, null, null);
        KubeflowConnectorRequest getRunByIdConnectorRequest = new KubeflowConnectorRequest(
                connectorRequest.authentication(), kubeflowApi);
        KubeflowConnectorExecutor getRunByIdExecutor = KubeflowConnectorExecutor.getExecutor(
                getRunByIdConnectorRequest,
                processInstanceKey);
        HttpCommonResult result = getRunByIdExecutor.execute(httpService);
        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) result.getBody();
        if (body.size() > 0) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) body
                    .get("run");
            String status = (String) map.get("status");
            return status;
        } else {
            throw new RuntimeException("Run with id '" + runId + "' could not ne found");
        }
    }
}
