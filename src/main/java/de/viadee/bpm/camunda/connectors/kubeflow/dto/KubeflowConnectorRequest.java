package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
    @Valid @NotNull Authentication authentication) {}
