package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

public record Configuration(
    String kubeflowUrl,
    String multiusernamespace) { 
    }
