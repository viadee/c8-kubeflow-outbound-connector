package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.http.HttpRequestFactory;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.feel.jackson.JacksonModuleFeelFunction;
import io.camunda.connector.generator.annotation.ElementTemplate;
import io.camunda.connector.http.base.components.HttpTransportComponentSupplier;
import io.camunda.connector.http.base.services.HttpService;

@OutboundConnector(
  name = "Kubeflow Connector", 
  inputVariables = { "authentication", "kubeflowapi" }, 
  type = "de.viadee.bpm.camunda:connector-kubeflow:1"
)
@ElementTemplate(
  id = "de.viadee.bpm.camunda.connectors.kubeflow.v1", 
  name = "Kubeflow connector", 
  version = 1, 
  description = "Connector for communicating with Kubeflow from Camunda", 
  icon = "icon.svg",
  documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/", 
  propertyGroups = {
    @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication")
  }, 
  inputDataClass = KubeflowConnectorRequest.class
)
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  public static final String TYPE = "de.viadee.bpm.camunda:connector-kubeflow:1";

  private final HttpService httpService;

  public KubeflowConnectorFunction() {
    this(
        JsonMapper.builder()
          .addModules(new JacksonModuleFeelFunction(), new Jdk8Module(), new JavaTimeModule())
          .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
          .build(),
        HttpTransportComponentSupplier.httpRequestFactoryInstance());
  }

  public KubeflowConnectorFunction(
      final ObjectMapper objectMapper, final HttpRequestFactory requestFactory) {
    this.httpService = new HttpService(objectMapper, requestFactory);
  }

  @Override
  public Object execute(final OutboundConnectorContext context)
      throws IOException, InstantiationException, IllegalAccessException {
        final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
        return httpService.executeConnectorRequest(connectorRequest.getHttpRequest());
  }
}
