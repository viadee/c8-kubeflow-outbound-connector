package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
        @Valid @NotNull Authentication authentication,
        @Valid @NotNull KubeflowApi kubeflowapi) {
}
