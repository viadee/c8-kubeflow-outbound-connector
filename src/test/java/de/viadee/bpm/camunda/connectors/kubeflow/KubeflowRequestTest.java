package de.viadee.bpm.camunda.connectors.kubeflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;

public class KubeflowRequestTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldReplaceTokenSecretWhenReplaceSecrets() throws JsonProcessingException {
    // given
    var input = new KubeflowConnectorRequest(
            "Hello World!",
            new Authentication("testUser", "secrets.MY_TOKEN")
    );
    var context = OutboundConnectorContextBuilder.create()
      .secret("MY_TOKEN", "token value")
            .variables(objectMapper.writeValueAsString(input))
      .build();
    // when
    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    // then
    assertThat(connectorRequest)
      .extracting("authentication")
      .extracting("token")
      .isEqualTo("token value");
  }

  @Test
  void shouldFailWhenValidate_NoAuthentication() throws JsonProcessingException {
    // given
    var input = new KubeflowConnectorRequest(
            "Hello World!",
            null
    );
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(
        KubeflowConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("authentication");
  }

  @Test
  void shouldFailWhenValidate_NoToken() throws JsonProcessingException {
    // given
    var input = new KubeflowConnectorRequest(
            "Hello World!",
            new Authentication("testUser", null)
    );
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(
        KubeflowConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("token");
  }

  @Test
  void shouldFailWhenValidate_NoMesage() throws JsonProcessingException {
    // given
    var input = new KubeflowConnectorRequest(
            null,
            new Authentication("testUser", "testToken")
    );
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(
        KubeflowConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("message");
  }

  @Test
  void shouldFailWhenValidate_TokenEmpty() throws JsonProcessingException {
    // given
    var input = new KubeflowConnectorRequest(
            "foo",
            new Authentication("testUser", "")
    );
    var context = OutboundConnectorContextBuilder.create().variables(objectMapper.writeValueAsString(input)).build();
    // when
    assertThatThrownBy(() -> context.bindVariables(
        KubeflowConnectorRequest.class))
      // then
      .isInstanceOf(ConnectorInputException.class)
      .hasMessageContaining("authentication.token: Validation failed");
  }
}