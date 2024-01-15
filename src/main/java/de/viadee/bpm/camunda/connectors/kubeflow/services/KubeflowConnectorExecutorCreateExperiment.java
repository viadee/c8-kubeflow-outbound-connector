package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import io.camunda.connector.feel.jackson.JacksonModuleFeelFunction;
import io.swagger.client.model.V1ApiExperiment;
import io.swagger.client.model.V1ApiResourceKey;
import io.swagger.client.model.V1ApiResourceReference;
import io.swagger.client.model.V1ApiResourceType;
import io.swagger.client.model.V2beta1Experiment;

public class KubeflowConnectorExecutorCreateExperiment extends KubeflowConnectorExecutor {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModules(new JacksonModuleFeelFunction(), new Jdk8Module(), new JavaTimeModule())
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
      .build();

    public KubeflowConnectorExecutorCreateExperiment(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    // @Override
    // protected void setHeaders(Builder httpRequestBuilder) {
    //     httpRequestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
    // }

    @Override
    protected BodyPublisher buildPayloadForKubeflowEndpoint() {
        Map<String, Object> payload = new HashMap<>();
        if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
            payload = getPayloadForEndpointV1();
        }
        payload = getPayloadForEndpointV2();
        try {
            return HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getPayloadForEndpointV1() {
        var v1ApiResourceReference = new V1ApiResourceReference()
            .key(new V1ApiResourceKey()
                .type(V1ApiResourceType.NAMESPACE)
                .id(super.kubeflowMultiNs));

        var v1ApiExperiment = new V1ApiExperiment()
            .name(getName())
            .description(getDescription())
            .addResourceReferencesItem(v1ApiResourceReference);

        return objectMapper.convertValue(v1ApiExperiment,
            new TypeReference<>() {});
    }

    private Map<String, Object> getPayloadForEndpointV2() {
        var v2Beta1Experiment = new V2beta1Experiment()
            .displayName(getName())
            .description(getDescription())
            .namespace(super.kubeflowMultiNs);
        return objectMapper.convertValue(v2Beta1Experiment,
            new TypeReference<>() {});
    }

    private String getDescription() {
        var description = connectorRequest.kubeflowapi().experimentDescription();
        if (description == null) {
            return "";
        }
        return description;
    }

    private String getName() {
        var name = connectorRequest.kubeflowapi().experimentName();
        if (name == null) {
            return "";
        }
        return name;
    }
}
