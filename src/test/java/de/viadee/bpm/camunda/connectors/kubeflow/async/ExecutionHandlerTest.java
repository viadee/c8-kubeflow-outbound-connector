package de.viadee.bpm.camunda.connectors.kubeflow.async;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExecutionHandlerTest {

  ExecutionHandler executionHandler = new ExecutionHandler();
  @ParameterizedTest
  @CsvSource({"get_pipelines, KubeflowConnectorExecutor",
      "get_experiments, KubeflowConnectorExecutor", "get_runs, KubeflowConnectorExecutor",
      "get_run_by_id, KubeflowConnectorExecutorGetRunById", "get_run_by_name, KubeflowConnectorExecutorGetRunByName",
      "start_run, KubeflowConnectorExecutorStartRun", "start_run_and_monitor, KubeflowConnectorExecutorStartRun",
      "create_experiment, KubeflowConnectorExecutorCreateExperiment"})
  void getExecutor(String pipelineOperation, String className) {
    // given
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(),
        new Configuration("http://localhost:8281", "multiUserMode"),
        new KubeflowApi("pipelinesV1", pipelineOperation, null, null,
            null, null, null, null, null, null, null, null, null),
        new Timeout(20)
    );
    // when
    var executor = executionHandler.getExecutor(kubeflowConnectorRequest, 123456789);
    // then
    assertEquals(className, executor.getClass().getSimpleName());
  }
}