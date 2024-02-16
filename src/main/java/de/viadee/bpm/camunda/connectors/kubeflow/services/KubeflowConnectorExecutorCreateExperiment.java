package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.JsonHelper;
import io.swagger.client.model.V1ApiExperiment;
import io.swagger.client.model.V1ApiResourceKey;
import io.swagger.client.model.V1ApiResourceReference;
import io.swagger.client.model.V1ApiResourceType;
import io.swagger.client.model.V2beta1Experiment;

public class KubeflowConnectorExecutorCreateExperiment extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorCreateExperiment(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    @Override
    protected BodyPublisher buildPayloadForKubeflowEndpoint() {
        if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
            try {
                return HttpRequest.BodyPublishers.ofString(JsonHelper.objectMapper.writeValueAsString(getPayloadForEndpointV1()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return HttpRequest.BodyPublishers.ofString(JsonHelper.objectMapper.writeValueAsString(getPayloadForEndpointV2()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        
    }

    private V1ApiExperiment getPayloadForEndpointV1() {
        var v1ApiResourceReference = new V1ApiResourceReference()
            .key(new V1ApiResourceKey()
                .type(V1ApiResourceType.NAMESPACE)
                .id(super.kubeflowMultiNs));

        var v1ApiExperiment = new V1ApiExperiment()
            .name(getName())
            .description(getDescription())
            .addResourceReferencesItem(v1ApiResourceReference);

        return v1ApiExperiment;
    }

    private Map<String, Object> getPayloadForEndpointV2() {
        var v2Beta1Experiment = new V2beta1Experiment()
            .displayName(getName())
            .description(getDescription())
            .namespace(super.kubeflowMultiNs);
        return JsonHelper.objectMapper.convertValue(v2Beta1Experiment,
            new TypeReference<>() {});
    }

    private String getDescription() {
        var description = connectorRequest.getKubeflowapi().experimentDescription();
        if (description == null) {
            return "";
        }
        return description;
    }

    private String getName() {
        var name = connectorRequest.getKubeflowapi().experimentName();
        if (name == null) {
            return "";
        }
        return name;
    }
}
