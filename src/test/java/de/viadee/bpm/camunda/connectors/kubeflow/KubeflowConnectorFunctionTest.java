package de.viadee.bpm.camunda.connectors.kubeflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;


class KubeflowConnectorFunctionTest {

  KubeflowConnectorFunction kubeflowConnectorFunction = new KubeflowConnectorFunction();
  ObjectMapper objectMapper = new ObjectMapper();
  KubeflowConnectorExecutor kubeflowConnectorExecutor;
  ExecutionHandler executionHandler;

  @BeforeEach
  public void setup() {
    kubeflowConnectorExecutor = mock(KubeflowConnectorExecutor.class);
    executionHandler = mock(ExecutionHandler.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"null, null", "'', ''", "'', 'get_pipelines'"}, nullValues = {"null"})
  void failOnExecuteWhenApiAndOperationEmpty(String api, String operation) throws Exception {
    // given
    var kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(),
        new Configuration("http://localhost:8281", "multiUserMode"),
        new KubeflowApi(api, operation, null, null,
            null, null, null, null, null, null, null, null, null),
        new Timeout(20)
    );
    var context = OutboundConnectorContextBuilder.create()
        .variables(objectMapper.writeValueAsString(kubeflowConnectorRequest))
        .build();
    // when
    var result = catchThrowable(() -> kubeflowConnectorFunction.execute(context));
    // then
    assertThat(result)
        .isInstanceOf(ConnectorInputException.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"pipelinesV3, create_pipeline", "foo1, foo2"})
  void failOnExecuteWhenApiAndOperationUnknown(String api, String operation) throws Exception {
    // given
    var kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(),
        new Configuration("http://localhost:8281", "multiUserMode"),
        new KubeflowApi(api, operation, null, null,
            null, null, null, null, null, null, null, null, null),
        new Timeout(20)
    );
    var context = OutboundConnectorContextBuilder.create()
        .variables(objectMapper.writeValueAsString(kubeflowConnectorRequest))
        .build();
    // when
    var result = catchThrowable(() -> kubeflowConnectorFunction.execute(context));
    // when(httpResponseMock.body()).thenReturn("{\"pipelines\":[{\"id\":\"123\",\"created_at\":\"2024-02-06T18:23:09Z\",\"name\":\"name1\",\"description\":\"description1\"},{\"id\":\"234\",\"created_at\":\"2024-02-06T18:23:10Z\",\"name\":\"name2\",\"description\":\"description2\"}],\"total_size\":2}");
    // then
    assertThat(result)
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @CsvSource(value = {"localhost:8080", "www.localhost:0", "http:/kubeflow:2020"})
  void failOnExecuteWhenConfigurationHasInvalidURL(String url) throws Exception {
    // given
    var kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(),
        new Configuration(url, "multiUserMode"),
        new KubeflowApi("pipelinesV1", "get_pipelines", null, null,
            null, null, null, null, null, null, null, null, null),
        new Timeout(20)
    );
    var context = OutboundConnectorContextBuilder.create()
        .variables(objectMapper.writeValueAsString(kubeflowConnectorRequest))
        .build();
    // when
    var result = catchThrowable(() -> kubeflowConnectorFunction.execute(context));
    // then
    assertThat(result)
        .isInstanceOf(IllegalArgumentException.class);
  }
}