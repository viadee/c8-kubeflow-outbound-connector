package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import org.apache.http.client.utils.URIBuilder;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApiOperationsEnum);
    }

    @Override
    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        var kubeflowUrilPath = String.format("%s/%s",
            kubeflowApiOperationsEnum.getApiUrl(),
            connectorRequest.kubeflowapi().runId());
        uriBuilder.setPath(kubeflowUrilPath);
    }
}
