package de.viadee.bpm.camunda.connectors.kubeflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorResult;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.annotation.ElementTemplate;

@OutboundConnector(
    name = "Kubeflow Connector",
    inputVariables = {"authentication", "kubeflowUrl", "kubeflowApi", "apiOperation"},
    type = "de.viadee.bpm.camunda:connector-kubeflow:1")
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
    inputDataClass = KubeflowConnectorRequest.class)
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(KubeflowConnectorFunction.class);

  @Override
  public Object execute(OutboundConnectorContext context) {
    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    return executeConnector(connectorRequest);
  }

  private KubeflowConnectorResult executeConnector(final KubeflowConnectorRequest connectorRequest) {
    // TODO: implement connector logic
    LOGGER.info("Executing my connector with request {}", connectorRequest);
    String cookievalue = connectorRequest.authentication().cookievalue();
    String kubeflowUrl = connectorRequest.authentication().kubeflowUrl();
    if (cookievalue != null && cookievalue.toLowerCase().startsWith("fail")) {
      throw new ConnectorException("FAIL", "My property started with 'fail', was: " + cookievalue);
    }
    return new KubeflowConnectorResult("Message received: " + cookievalue);
  }
}
