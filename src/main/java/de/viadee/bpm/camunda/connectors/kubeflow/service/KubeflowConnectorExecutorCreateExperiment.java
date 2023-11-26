package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import java.util.Map;

public class KubeflowConnectorExecutorCreateExperiment extends KubeflowConnectorExecutor {

    private static final String IN_EXPERIMENT_NAME = "display_name";
    private static final String IN_EXPERIMENT_NAMESPACE = "namespace";
    private static final String IN_EXPERIMENT_DESCRIPTION = "description";

    public KubeflowConnectorExecutorCreateExperiment(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApiOperationsEnum);
    }

    @Override
    protected Object buildPayloadForKubeflowEndpoint() {
        return Map.of(
            IN_EXPERIMENT_NAME, connectorRequest.kubeflowapi().experimentName(),
            IN_EXPERIMENT_NAMESPACE, super.kubeflowMultiNs,
            IN_EXPERIMENT_DESCRIPTION, connectorRequest.kubeflowapi().experimentDescription()
        );
    }
}
