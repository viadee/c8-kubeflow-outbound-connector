package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import io.camunda.connector.http.base.auth.Authentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest(
        @Valid Authentication authentication,
        @Valid Configuration configuration,
        @Valid @NotNull KubeflowApi kubeflowapi) {
}
