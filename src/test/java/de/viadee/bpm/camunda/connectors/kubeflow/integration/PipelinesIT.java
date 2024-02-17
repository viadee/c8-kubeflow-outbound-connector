package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PipelinesIT extends BaseIntegrationTest {

  @ParameterizedTest
  @CsvSource({"pipelinesV1", "pipelinesV2"})
  public void testGetPipelines(String pipelineVersion) throws Exception {
    // when
    HttpResponse<String> response = getPipelines(pipelineVersion);
    // then
    Assertions.assertEquals(200, response.statusCode());
  }
}