package de.viadee.bpm.camunda.connectors.kubeflow.enums;

import java.util.Arrays;

public enum KubeflowApiOperationsEnum {
        GET_PIPELINES("get_pipelines", "GET", false,
                "/pipeline/apis/%s/pipelines"),
        GET_EXPERIMENTS("get_experiments", "GET", true,
                "/pipeline/apis/%s/experiments"),
        GET_RUNS("get_runs", "GET", true,
                "/pipeline/apis/%s/runs"),
        GET_RUN_BY_ID("get_run_by_id", "GET", false,
                "/pipeline/apis/%s/runs"),
        GET_RUN_BY_NAME("get_run_by_name", "GET", true,
                "/pipeline/apis/%s/runs"),
        START_RUN("start_run", "POST", false,
                "/pipeline/apis/%s/runs"),
        START_RUN_AND_MONITOR("start_run_and_monitor","POST", false,
                "/pipeline/apis/%s/runs"),
        CREATE_EXPERIMENT("create_experiment", "POST", true,
            "/pipeline/apis/%s/experiments");

        private final String value;
        private final String httpMethod;
        private final boolean isNamespaceFilterRequired;
        private final String apiUrl;

        KubeflowApiOperationsEnum(String value, String httpMethod, boolean isNamespaceFilterRequired, String apiUrl) {
            this.value = value;
            this.httpMethod = httpMethod;
            this.isNamespaceFilterRequired = isNamespaceFilterRequired;
            this.apiUrl = apiUrl;
        }

        public String getValue() {
            return value;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public boolean isNamespaceFilterRequired() {
            return isNamespaceFilterRequired;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public static KubeflowApiOperationsEnum fromValue(String value) {
            return Arrays
                .stream(values())
                .filter(kubeflowApiOperationsEnum -> kubeflowApiOperationsEnum.value.equals(value))
                .findFirst()
                .orElseThrow(
                    () -> new IllegalArgumentException("Unbekannter Wert: " + value)
                );
        }
    }