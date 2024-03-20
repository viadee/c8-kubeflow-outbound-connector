package de.viadee.bpm.camunda.connectors.kubeflow.services.async;

import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorGetExperimentById;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorCreateExperiment;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorGetRunById;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorGetRunByName;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorStartRun;

public class ExecutionHandler {

    public static KubeflowConnectorExecutor getExecutor(KubeflowConnectorRequest connectorRequest,
            long processInstanceKey) {

        var selectedApi = KubeflowApisEnum.fromValue(
            connectorRequest.getKubeflowapi().api()
        );

        var selectedOperation = KubeflowApiOperationsEnum.fromValue(
            connectorRequest.getKubeflowapi().operation()
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
            case GET_EXPERIMENT_BY_ID:
                return new KubeflowConnectorExecutorGetExperimentById(connectorRequest, processInstanceKey, selectedApi,
                    KubeflowApiOperationsEnum.GET_EXPERIMENT_BY_ID);
            default: // OTHER
                throw new RuntimeException("Selected operation is not supported");
        }
    }

    public static <T> HttpResponse<String> runCallableAfterDelay(Callable<HttpResponse<String>> task, long delay, TimeUnit timeUnit)
            throws InterruptedException, ExecutionException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        try {
            // Schedule the callable task to run after the specified delay
            ScheduledFuture<HttpResponse<String>> future = scheduler.schedule(task, delay, timeUnit);

            // Wait for the callable to finish and retrieve the result
            HttpResponse<String> result = future.get(); // This blocks until the result is available

            // Use the result
            return result;
        } finally {
            scheduler.shutdown(); // It's important to shut down the executor service to avoid resource leaks
        }
    }
}
