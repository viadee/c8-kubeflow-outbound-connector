package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

import io.camunda.connector.generator.java.annotation.TemplateProperty;

public record Timeout(
    @TemplateProperty(group = "timeout", defaultValue = "20", optional = false, description = "Sets the timeout in seconds to establish a connection or 0 for an infinite timeout")
    Integer connectionTimeoutInSeconds) { 
}
