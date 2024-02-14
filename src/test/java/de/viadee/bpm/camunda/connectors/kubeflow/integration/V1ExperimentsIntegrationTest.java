package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutorCreateExperiment;
import io.camunda.connector.http.base.model.HttpCommonResult;

public class V1ExperimentsIntegrationTest extends BaseIntegrationTest {

  private void createExperiment(String experimentName) throws Exception {
    KubeflowApi kubeflowApi = new KubeflowApi(null, null, null, null, null, null, null, experimentName, null);
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(this.getConfiguration(),
        kubeflowApi);
    HttpCommonResult result = new KubeflowConnectorExecutorCreateExperiment(
        kubeflowConnectorRequest, 0,
        KubeflowApisEnum.PIPELINES_V1, KubeflowApiOperationsEnum.CREATE_EXPERIMENT)
        .execute(createHttpService());

    Assertions.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus());
  }

  private List<String> getExperiments() throws Exception {
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(this.getConfiguration(),
        new KubeflowApi(null, null, null, null, null, null, null, null, null));

    HttpCommonResult result = new KubeflowConnectorExecutorCreateExperiment(
        kubeflowConnectorRequest, 0,
        KubeflowApisEnum.PIPELINES_V1, KubeflowApiOperationsEnum.GET_EXPERIMENTS)
        .execute(createHttpService());

    // Assert response code is 200
    Assertions.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus());

    List<String> experiments = new ArrayList<>();
    for (Map<String, String> experiment : ((Map<String, List<Map<String, String>>>) result.getBody())
        .get("experiments")) {
      experiments.add(experiment.get("name"));
    }
    return experiments;
  }

  @Test
  public void testCreateExperiment() throws Exception {
    // Generate an experiment name
    String experimentName = "my-new-experiment-" + RandomStringUtils.randomAlphanumeric(10);

    createExperiment(experimentName);

    List<String> experiments = getExperiments();
    Assertions.assertTrue(experiments.contains(experimentName));
  }
}