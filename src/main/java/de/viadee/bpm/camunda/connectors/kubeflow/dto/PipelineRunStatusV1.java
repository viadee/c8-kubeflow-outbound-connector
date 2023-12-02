package de.viadee.bpm.camunda.connectors.kubeflow.dto;

public enum PipelineRunStatusV1 {
  SUCCEEDED("Succeeded"),
  FAILED("Failed"),
  PENDING("Pending"),
  RUNNING("Running"),
  SKIPPED("Skipped"),
  ERROR("Error");

  private final String value;

  PipelineRunStatusV1(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
