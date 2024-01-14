package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.annotation.ElementTemplate;

@OutboundConnector(name = "Kubeflow Connector", inputVariables = { "authentication", "configuration",
    "kubeflowapi" }, type = "de.viadee.bpm.camunda:connector-kubeflow:1")
@ElementTemplate(id = "de.viadee.bpm.camunda.connectors.kubeflow.v1", name = "Kubeflow connector", version = 1, description = "Connector for communicating with Kubeflow from Camunda", icon = "icon.svg", documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/", propertyGroups = {
    @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
    @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration"),
    @ElementTemplate.PropertyGroup(id = "kubeflowapi", label = "Kubeflow API")
}, inputDataClass = KubeflowConnectorRequest.class)
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  private final HttpClient httpClient;
  private static final ObjectMapper objectMapper = JsonMapper.builder()
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
    this.httpClient = HttpClient.newBuilder().proxy(new ProxySelector() {

      @Override
      public void connectFailed(URI arg0, SocketAddress arg1, IOException arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'connectFailed'");
      }

      @Override
      public List<Proxy> select(URI arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'select'");
      }

    }).build();
  }

  @Override
  public Object execute(final OutboundConnectorContext context)
      throws IOException, InstantiationException, IllegalAccessException {
    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    long processInstanceKey = context.getJobContext().getProcessInstanceKey();

    KubeflowConnectorExecutor connectorExecutor = ExecutionHandler.getExecutor(connectorRequest, processInstanceKey);
    HttpResponse<String> httpResponse = connectorExecutor.execute(httpClient);
    return httpResponse;
  }
}
