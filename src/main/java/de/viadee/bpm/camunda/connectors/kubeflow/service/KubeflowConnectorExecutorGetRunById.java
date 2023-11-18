package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        super(connectorRequest, processInstanceKey);
    }

    @Override
    protected String buildKubeflowUrlPath() {
        return KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation()).getApiUrl()+"/"+connectorRequest.kubeflowapi().runid();
    }
}
