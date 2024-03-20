package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

import jakarta.validation.constraints.NotEmpty;

public record Configuration(
    String kubeflowUrl,
    @NotEmpty
    String typeOfUserMode) {
    }
