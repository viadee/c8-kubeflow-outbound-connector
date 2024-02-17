package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.OffsetDateTimeDeserializer;
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
import org.threeten.bp.OffsetDateTime;

public class ExperimentsIT extends BaseIntegrationTest {

  private static ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class,
          new OffsetDateTimeDeserializer()))
      .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

  private void createExperiment(String experimentName, String pipelineVersion) throws Exception {
    getExecutor(pipelineVersion, "create_experiment", experimentName).execute();
  }

  private List<String> getExperiments(String pipelineVersion) throws Exception {
    HttpResponse<String> result = getExecutor(pipelineVersion,
        "get_experiments", null).execute();

    var listOfExperimentNames = KubeflowApisEnum.PIPELINES_V1.getValue().equals(pipelineVersion) ?
        objectMapper.readValue(result.body(), V1ApiListExperimentsResponse.class)
            .getExperiments()
            .stream()
            .map(V1ApiExperiment::getName).collect(Collectors.toList()) :
        objectMapper.readValue(result.body(), V2beta1ListExperimentsResponse.class)
            .getExperiments()
            .stream()
            .map(V2beta1Experiment::getDisplayName).collect(Collectors.toList());

    return listOfExperimentNames;
  }

  @ParameterizedTest
  @CsvSource({"pipelinesV1", "pipelinesV2"})
  public void testCreateAndGetExperiment(String pipelineVersion) throws Exception {
    // Generate an experiment name
    String experimentName = "my-new-experiment-" + RandomStringUtils.randomAlphanumeric(10);

    // create experiment
    createExperiment(experimentName, pipelineVersion);

    // get experiment
    List<String> experiments = getExperiments(pipelineVersion);

    // check if experiment is part of list of experiments
    Assertions.assertTrue(experiments.contains(experimentName));
  }
}