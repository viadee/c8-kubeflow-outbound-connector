package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, Long processInstanceKey) {
        super(connectorRequest, processInstanceKey);
    }

    @Override
    protected String buildKubeflowUrlPath() {
        return String.format("%s/%s",
            KubeflowApiOperationsEnum.GET_RUN_BY_ID.getApiUrl(),
            connectorRequest.kubeflowapi().runid());
    }
}
