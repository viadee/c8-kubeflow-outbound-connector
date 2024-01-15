package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.feel.jackson.JacksonModuleFeelFunction;
import io.camunda.connector.generator.annotation.ElementTemplate;

@OutboundConnector(name = "Kubeflow Connector", inputVariables = { "authentication", "configuration",
    "kubeflowapi" }, type = "de.viadee.bpm.camunda:connector-kubeflow:1")
@ElementTemplate(id = "de.viadee.bpm.camunda.connectors.kubeflow.v1", name = "Kubeflow connector", version = 2, description = "Connector for communicating with Kubeflow from Camunda", icon = "icon.svg", documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/", propertyGroups = {
    @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
    @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration"),
    @ElementTemplate.PropertyGroup(id = "kubeflowapi", label = "Kubeflow API")
}, inputDataClass = KubeflowConnectorRequest.class)
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  private final HttpClient httpClient;
  private static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModules(new JacksonModuleFeelFunction(), new Jdk8Module(), new JavaTimeModule())
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
      .build();

  public KubeflowConnectorFunction() {
    this(
        objectMapper.copy());
  }

  public KubeflowConnectorFunction(
      final ObjectMapper objectMapper) {
    this.httpClient = HttpClient.newBuilder().build();
  }

  @Override
  public Object execute(final OutboundConnectorContext context)
      throws IOException, InstantiationException, IllegalAccessException {
    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    long processInstanceKey = context.getJobContext().getProcessInstanceKey();

    KubeflowConnectorExecutor connectorExecutor = ExecutionHandler.getExecutor(connectorRequest, processInstanceKey);
    return connectorExecutor.execute(httpClient);
  }
}
