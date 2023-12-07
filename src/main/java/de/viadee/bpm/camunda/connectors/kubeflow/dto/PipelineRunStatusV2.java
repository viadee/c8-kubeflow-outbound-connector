package de.viadee.bpm.camunda.connectors.kubeflow.dto;

public enum PipelineRunStatusV2 {
  SUCCEEDED,
  FAILED,
  PENDING,
  RUNNING,
  SKIPPED,
  CANCELED,
  PAUSED;
}
