package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;

@OutboundConnector(name = "Kubeflow Connector", inputVariables = { "authentication", "configuration",
    "kubeflowapi", "timeout" }, type = "de.viadee.bpm.camunda:connector-kubeflow:1")
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  public KubeflowConnectorFunction() {
  }

  @Override
  public Object execute(final OutboundConnectorContext context)
      throws IOException, InstantiationException, IllegalAccessException {
    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    long processInstanceKey = context.getJobContext().getProcessInstanceKey();

    KubeflowConnectorExecutor connectorExecutor = ExecutionHandler.getExecutor(connectorRequest, processInstanceKey);

    HttpResponse<String> response = connectorExecutor.execute();

    // raise error based on status code
    if (response.statusCode() >= 300) {
      throw new RuntimeException(response.body());
    }

    ObjectMapper mapper = new ObjectMapper();
    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
    };
    return mapper.readValue(response.body(), typeRef);
  }
}
