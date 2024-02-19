package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import io.swagger.client.model.V1ApiExperiment;
import io.swagger.client.model.V1ApiListExperimentsResponse;
import io.swagger.client.model.V2beta1Experiment;
import io.swagger.client.model.V2beta1ListExperimentsResponse;
import java.net.http.HttpResponse;
import java.util.List;

import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ExperimentsIT extends BaseIntegrationTest {

  private void createExperiment(String pipelineVersion, String experimentName) throws Exception {
    getExecutor(pipelineVersion, "create_experiment", experimentName, null,
        null, null).execute();
  }

  private List<String> getNamesOfExperiments(String pipelineVersion) throws Exception {
    HttpResponse<String> response = getExecutor(pipelineVersion,
        "get_experiments", null, null, null, null).execute();

    var listOfExperimentNames = KubeflowApisEnum.PIPELINES_V1.getValue().equals(pipelineVersion) ?
        objectMapper.readValue(response.body(), V1ApiListExperimentsResponse.class)
            .getExperiments()
            .stream()
            .map(V1ApiExperiment::getName).collect(Collectors.toList()) :
        objectMapper.readValue(response.body(), V2beta1ListExperimentsResponse.class)
            .getExperiments()
            .stream()
            .map(V2beta1Experiment::getDisplayName).collect(Collectors.toList());

    return listOfExperimentNames;
  }

  @ParameterizedTest
  @CsvSource({"pipelinesV1", "pipelinesV2"})
  public void testCreateAndGetExperiments(String pipelineVersion) throws Exception {
    // Generate an experiment name
    String experimentName = "my-new-experiment-" + RandomStringUtils.randomAlphanumeric(10);

    // create experiment
    createExperiment(pipelineVersion, experimentName);

    // get all experiment names
    List<String> experiments = getNamesOfExperiments(pipelineVersion);

    // check if experiment is part of list of experiments
    Assertions.assertTrue(experiments.contains(experimentName));
  }
}