package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;

public class KubeflowConnectorExecutorGetRunByName extends KubeflowConnectorExecutor {

  public KubeflowConnectorExecutorGetRunByName(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
    super(connectorRequest, processInstanceKey);
  }

  @Override
  protected String getFilterString() {
    String filter = "{\"predicates\": [{\"op\": \"EQUALS\",\"key\": \"name\", \"string_value\": \"" + processInstanceKey + "\"}]}";
    return filter;
  }
}
