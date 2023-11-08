package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import io.camunda.connector.generator.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record Authentication(
    @NotEmpty
    @TemplateProperty(group = "authentication", label = "Cookievalue", description = "Cookievalue for authentication (for testing)")
    String cookievalue,
    @TemplateProperty(group = "authentication", label = "Kubeflow URL", description = "URL of Kubeflow")
    String kubeflowUrl) { 
    }
