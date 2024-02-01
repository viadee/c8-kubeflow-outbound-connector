package de.viadee.bpm.camunda.connectors.kubeflow.entities;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
        @Valid Authentication authentication,
        @Valid Configuration configuration,
        @Valid @NotNull KubeflowApi kubeflowapi,
        @Valid @NotNull Integer connectionTimeoutInSeconds) {
}
