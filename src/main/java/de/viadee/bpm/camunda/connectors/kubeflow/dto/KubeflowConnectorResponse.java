package de.viadee.bpm.camunda.connectors.kubeflow.dto;

public interface KubeflowConnectorResponse {
    public static String RUN_STATUS_SUCCEEDED = "Succeeded";
    public static String RUN_STATUS_FAILED = "Failed";
    public static String RUN_STATUS_PENDING = "Pending";
    public static String RUN_STATUS_RUNNING = "Running";
    public static String RUN_STATUS_SKIPPED = "Skipped";
    public static String RUN_STATUS_ERROR = "Error";
}
