package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorCreateExperiment;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunById;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunByName;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorStartRun;

public class ExecutionHandler {

    public static KubeflowConnectorExecutor getExecutor(KubeflowConnectorRequest connectorRequest,
            long processInstanceKey) {

        var selectedApi = KubeflowApisEnum.fromValue(
            connectorRequest.kubeflowapi().api()
        );

        var selectedOperation = KubeflowApiOperationsEnum.fromValue(
            connectorRequest.kubeflowapi().operation()
        );

        switch (selectedOperation) {
            case GET_RUN_BY_ID:
                return new KubeflowConnectorExecutorGetRunById(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_RUN_BY_ID);
            case GET_RUN_BY_NAME:
                return new KubeflowConnectorExecutorGetRunByName(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_RUN_BY_NAME);
            case START_RUN:
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.START_RUN);
            case START_RUN_AND_MONITOR:
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.START_RUN_AND_MONITOR);
            case GET_RUNS:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_RUNS);
            case GET_EXPERIMENTS:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_EXPERIMENTS);
            case GET_PIPELINES:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_PIPELINES);
            case CREATE_EXPERIMENT:
                return new KubeflowConnectorExecutorCreateExperiment(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.CREATE_EXPERIMENT);
            default: // OTHER
                throw new RuntimeException("Selected operation is not supported");
        }
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
}
