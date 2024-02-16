package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

public record Timeout(
    Integer connectionTimeoutInSeconds) { 
}
