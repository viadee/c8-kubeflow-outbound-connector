package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunById;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunByName;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorStartRun;
import io.camunda.connector.http.base.model.HttpCommonResult;

public class ExecutionHandler {
    public static KubeflowConnectorExecutor getExecutor(KubeflowConnectorRequest connectorRequest,
            long processInstanceKey) {

        var selectedOperation = KubeflowApiOperationsEnum.fromValue(
            connectorRequest.kubeflowapi().operation()
        );

        switch (selectedOperation) {
            case GET_RUN_BY_ID:
                return new KubeflowConnectorExecutorGetRunById(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.GET_RUN_BY_ID);
            case GET_RUN_BY_NAME:
                return new KubeflowConnectorExecutorGetRunByName(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.GET_RUN_BY_NAME);
            case START_RUN:
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.START_RUN);
            case START_RUN_AND_MONITOR:
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.START_RUN_AND_MONITOR);
            case GET_RUNS:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.GET_RUNS);
            case GET_EXPERIMENTS:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.GET_EXPERIMENTS);
            case GET_PIPELINES:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey,
                    KubeflowApiOperationsEnum.GET_PIPELINES);
            default: // OTHER
                throw new RuntimeException("Selected operation is not supported");
        }
    }

    public static String getFieldFromGetRunResponse(HttpCommonResult httpCommonResult, String field)
            throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(httpCommonResult));
        json.hasNonNull("body");
        if (json.hasNonNull("body")
                && json.path("body").hasNonNull("run")
                && json.path("body").path("run").hasNonNull(field)) {
            String fieldValue = json.path("body").path("run").get(field).asText();
            return fieldValue;
        } else {
            throw new RuntimeException("unexpected result from kubeflow get run API request");
        }
    }

    public static String getFieldFromGetRunsResponse(HttpCommonResult httpCommonResult, String field)
            throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(httpCommonResult));
        json.hasNonNull("body");
        if (json.hasNonNull("body")
                && json.path("body").hasNonNull("runs")
                && json.path("body").get("runs").size() > 0
                && json.path("body").path("runs").get(0).hasNonNull(field)) {
                String fieldValue = json.path("body").path("runs").get(0).get(field).asText();
                return fieldValue;
        }
        return null;
    }

    public static <T> T runCallableAfterDelay(Callable<T> task, long delay, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException {
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

    public static <T> T runCallableImmediately(Callable<T> task) throws InterruptedException, ExecutionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            // Schedule the callable task to run after the specified delay
            Future<T> future = scheduler.submit(task);

            // Wait for the callable to finish and retrieve the result
            T result = future.get(); // This blocks until the result is available

            // Use the result
            return result;
        } finally {
            scheduler.shutdown(); // It's important to shut down the executor service to avoid resource leaks
        }
    }
}
