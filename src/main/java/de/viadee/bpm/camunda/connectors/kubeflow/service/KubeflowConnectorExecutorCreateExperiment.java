package de.viadee.bpm.camunda.connectors.kubeflow.service;

import java.util.Map;
import java.util.Arrays;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;


public class KubeflowConnectorExecutorCreateExperiment extends KubeflowConnectorExecutor {

    private static final String IN_EXPERIMENT_NAME = "name";
    private static final String IN_EXPERIMENT_NAMESPACE = "NAMESPACE";
    private static final String IN_EXPERIMENT_RESOURCE_REFERENCES = "resource_references";
    private static final String IN_EXPERIMENT_DESCRIPTION = "description";

    public KubeflowConnectorExecutorCreateExperiment(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApiOperationsEnum);
    }

    @Override
    protected Object buildPayloadForKubeflowEndpoint() {
        return Map.of(
            IN_EXPERIMENT_NAME, connectorRequest.kubeflowapi().experimentName(),
            IN_EXPERIMENT_RESOURCE_REFERENCES, Arrays.asList(Map.of("key", Map.of("type", IN_EXPERIMENT_NAMESPACE, "id", super.kubeflowMultiNs))),
            IN_EXPERIMENT_DESCRIPTION, connectorRequest.kubeflowapi().experimentDescription()
        );
    }
}
