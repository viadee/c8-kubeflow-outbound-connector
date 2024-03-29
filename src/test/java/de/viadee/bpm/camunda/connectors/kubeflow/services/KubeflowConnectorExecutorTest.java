package de.viadee.bpm.camunda.connectors.kubeflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.EnvironmentAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KubeflowConnectorExecutorTest {

  KubeflowApi kubeflowApiMock;

  @BeforeEach
  public void setup() {
    kubeflowApiMock = mock(KubeflowApi.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"null", "''", "' '"}, nullValues = {"null"})
  void readConfigurationParameterFromSystemEnvWhenBlank(String kubeflowUrl) {
    // given
    var kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(),
        new Configuration(kubeflowUrl, "multiUserMode"),
        kubeflowApiMock,
        new Timeout(20)
    );
    // when
    KubeflowConnectorExecutor kubeflowConnectorExecutor = new KubeflowConnectorExecutor(kubeflowConnectorRequest, 123456789,
        KubeflowApisEnum.PIPELINES_V1, KubeflowApiOperationsEnum.GET_PIPELINES);
    // then
    assertEquals("http://localhost:8080", kubeflowConnectorExecutor.kubeflowUrl); // env. configured in pom.xml via surefire plugin
  }
}