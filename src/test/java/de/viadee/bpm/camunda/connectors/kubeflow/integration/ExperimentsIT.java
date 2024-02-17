package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.OffsetDateTimeDeserializer;
import io.swagger.client.model.V1ApiExperiment;
import io.swagger.client.model.V1ApiListExperimentsResponse;
import io.swagger.client.model.V2beta1Experiment;
import io.swagger.client.model.V2beta1ListExperimentsResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.threeten.bp.OffsetDateTime;

public class ExperimentsIT extends BaseIntegrationTest {

  private static ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class,
          new OffsetDateTimeDeserializer()))
      .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

  private KubeflowConnectorExecutor getExecutor(String pipelineVersion, String operation, String experimentName)
      throws IOException, URISyntaxException, InterruptedException {
    var httpHeaders = Map.of("Cookie", this.getCookie());
    KubeflowApi kubeflowApi = new KubeflowApi(pipelineVersion, operation, null, null,
        null, null, null, null, null, experimentName, null, httpHeaders);
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(), // Authentication via Headers
        this.getConfiguration(),
        kubeflowApi,
        new Timeout(20));
    return ExecutionHandler.getExecutor(kubeflowConnectorRequest, 0);
  }

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
  public void testCreateExperimentV1(String pipelineVersion) throws Exception {
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