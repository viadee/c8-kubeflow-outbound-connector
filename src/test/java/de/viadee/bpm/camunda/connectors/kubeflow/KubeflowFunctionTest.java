package de.viadee.bpm.camunda.connectors.kubeflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorResult;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;

public class KubeflowFunctionTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReturnReceivedMessageWhenExecute() throws Exception {
    // given
    var input = new KubeflowConnectorRequest(
            "Hello World!",
            new Authentication("testUser", "testToken")
    );
    var function = new KubeflowConnectorFunction();
    var context = OutboundConnectorContextBuilder.create()
      .variables(objectMapper.writeValueAsString(input))
      .build();
    // when
    var result = function.execute(context);
    // then
    assertThat(result)
      .isInstanceOf(KubeflowConnectorResult.class)
      .extracting("myProperty")
      .isEqualTo("Message received: Hello World!");
  }

  @Test
  void shouldThrowWithErrorCodeWhenMessageStartsWithFail() throws Exception {
    // given
    var input = new KubeflowConnectorRequest(
            "Fail: unauthorized",
            new Authentication("testUser", "testToken")
    );
    var function = new KubeflowConnectorFunction();
    var context = OutboundConnectorContextBuilder.create()
        .variables(objectMapper.writeValueAsString(input))
        .build();
    // when
    var result = catchThrowable(() -> function.execute(context));
    // then
    assertThat(result)
        .isInstanceOf(ConnectorException.class)
        .hasMessageContaining("started with 'fail'")
        .extracting("errorCode").isEqualTo("FAIL");
  }
}