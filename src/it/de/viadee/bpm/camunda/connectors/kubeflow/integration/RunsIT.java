package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import io.swagger.client.model.V1ApiExperiment;
import io.swagger.client.model.V1ApiListPipelinesResponse;
import io.swagger.client.model.V1ApiListRunsResponse;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Experiment;
import io.swagger.client.model.V2beta1ListRunsResponse;
import io.swagger.client.model.V2beta1Run;

public class RunsIT extends BaseIntegrationTest {

  private void createRun(String pipelineVersion, String runName, String pipelineId, String experimentId)
      throws Exception {
    getExecutor(pipelineVersion, "start_run", null, pipelineId, experimentId, runName).execute();
  }

  private List<String> getNamesOfRuns(String pipelineVersion) throws Exception {
    HttpResponse<String> response = getExecutor(pipelineVersion, "get_runs", null,
        null, null, null).execute();

    var listOfRunNames = KubeflowApisEnum.PIPELINES_V1.getValue().equals(pipelineVersion) ?
        objectMapper.readValue(response.body(), V1ApiListRunsResponse.class)
            .getRuns()
            .stream()
            .map(V1ApiRun::getName).collect(Collectors.toList()) :
        objectMapper.readValue(response.body(), V2beta1ListRunsResponse.class)
            .getRuns()
            .stream()
            .map(V2beta1Run::getDisplayName).collect(Collectors.toList());

    return listOfRunNames;
  }

  private String createExperimentAndGetId(String pipelineVersion, String experimentName)
      throws Exception {
    HttpResponse<String> response = getExecutor(pipelineVersion, "create_experiment", experimentName, null,
        null, null).execute();

    var id = KubeflowApisEnum.PIPELINES_V1.getValue().equals(pipelineVersion) ?
        objectMapper.readValue(response.body(), V1ApiExperiment.class).getId() :
        objectMapper.readValue(response.body(), V2beta1Experiment.class).getExperimentId();

    return id;
  }

  @ParameterizedTest
  @CsvSource({"pipelinesV1", "pipelinesV2"})
  public void testCreateAndGetRuns(String pipelineVersion) throws Exception {
    // given
    String pipelineId = objectMapper.readValue(getPipelines("pipelinesV1").body(),
        V1ApiListPipelinesResponse.class).getPipelines().get(0).getId();
    String experimentName = "experiment-for-run-" + RandomStringUtils.randomAlphanumeric(10);
    String experimentId = createExperimentAndGetId(pipelineVersion, experimentName);

    // Generate a run name
    String runName = "new-run-name" + RandomStringUtils.randomAlphanumeric(10);

    // create run
    createRun(pipelineVersion, runName, pipelineId, experimentId);

    // get all run names
    List<String> runs = getNamesOfRuns(pipelineVersion);

    // check if run is part of list of runs
    Assertions.assertTrue(runs.contains(PROCESS_INSTANCE_ID + "_" + runName));
  }
}