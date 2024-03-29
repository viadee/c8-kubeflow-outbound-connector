package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

import java.util.Map;

import io.camunda.connector.feel.annotation.FEEL;
import jakarta.validation.constraints.NotEmpty;

public record KubeflowApi(
    @NotEmpty
    String api,

    @NotEmpty
    String operation,

    String runId,

    String runName,

    Map<String, Object> runParameters,

    String pipelineId,

    String experimentId,

    @FEEL
    String filter,

    String pollingInterval,

    String experimentName,

    String experimentDescription,

    Map<String, String> httpHeaders,

    String namespace

) { }
