package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import org.apache.http.client.utils.URIBuilder;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    @Override
    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        var kubeflowUrlPath = String.format("%s/%s",
            String.format(kubeflowApiOperationsEnum.getApiUrl(), kubeflowApisEnum.getUrlPathVersion()),
            connectorRequest.kubeflowapi().runId());
        uriBuilder.setPath(kubeflowUrlPath);
    }
}
