package de.viadee.bpm.camunda.connectors.kubeflow.service;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import java.util.Map;
import java.util.Arrays;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;


public class KubeflowConnectorExecutorCreateExperiment extends KubeflowConnectorExecutor {

    private static final String IN_EXPERIMENT_NAME_V1 = "name";
    private static final String IN_EXPERIMENT_NAME_V2 = "display_name";
    private static final String IN_EXPERIMENT_NAMESPACE_V1 = "NAMESPACE";
    private static final String IN_EXPERIMENT_NAMESPACE_V2 = "namespace";
    private static final String IN_EXPERIMENT_RESOURCE_REFERENCES_V1 = "resource_references";
    private static final String IN_EXPERIMENT_DESCRIPTION = "description";


    public KubeflowConnectorExecutorCreateExperiment(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    @Override
    protected Object buildPayloadForKubeflowEndpoint() {
        if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
            return getPayloadForEndpointV1();
        }
        return getPayloadForEndpointV2();
    }

    private Map<String, Object> getPayloadForEndpointV1() {
        return Map.of(
            IN_EXPERIMENT_NAME_V1, connectorRequest.kubeflowapi().experimentName(),
            IN_EXPERIMENT_RESOURCE_REFERENCES_V1, Arrays.asList(Map.of("key", Map.of("type",
                IN_EXPERIMENT_NAMESPACE_V1, "id", super.kubeflowMultiNs))),
            IN_EXPERIMENT_DESCRIPTION, getDescriptionOfExperiment()
        );
    }

    private Map<String, String> getPayloadForEndpointV2() {
        return Map.of(
            IN_EXPERIMENT_NAME_V2, connectorRequest.kubeflowapi().experimentName(),
            IN_EXPERIMENT_NAMESPACE_V2, super.kubeflowMultiNs,
            IN_EXPERIMENT_DESCRIPTION, getDescriptionOfExperiment()
        );
    }

    private String getDescriptionOfExperiment() {
        var description = connectorRequest.kubeflowapi().experimentDescription();
        if (description == null) {
            return "";
        }
        return description;
    }
}
