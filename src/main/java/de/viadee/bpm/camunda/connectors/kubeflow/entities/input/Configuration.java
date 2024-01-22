package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

import io.camunda.connector.generator.annotation.TemplateProperty;

public record Configuration(
    @TemplateProperty(group = "configuration", label = "Kubeflow URL", description = "URL of Kubeflow")
    String kubeflowUrl,
    @TemplateProperty(group = "configuration", label = "Kubeflow Namespace", description = "Kubeflow Namespace for Multi-User Isolation")
    String multiusernamespace) { 
    }
