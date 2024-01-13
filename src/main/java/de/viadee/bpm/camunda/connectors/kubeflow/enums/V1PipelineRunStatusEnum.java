package de.viadee.bpm.camunda.connectors.kubeflow.enums;

public enum V1PipelineRunStatusEnum {
  SUCCEEDED("Succeeded"),
  FAILED("Failed"),
  PENDING("Pending"),
  RUNNING("Running"),
  SKIPPED("Skipped"),
  ERROR("Error");

  private final String value;

  V1PipelineRunStatusEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
