package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
        @Valid Configuration configuration,
        @Valid @NotNull KubeflowApi kubeflowapi) {
}
