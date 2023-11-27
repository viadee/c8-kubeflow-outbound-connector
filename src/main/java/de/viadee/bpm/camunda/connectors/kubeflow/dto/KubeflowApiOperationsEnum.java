package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import io.camunda.connector.http.base.model.HttpMethod;
import java.util.Arrays;

public enum KubeflowApiOperationsEnum {
        GET_PIPELINES("get_pipelines", HttpMethod.GET, false,
                "/pipeline/apis/v1beta1/pipelines"),
        GET_EXPERIMENTS("get_experiments", HttpMethod.GET, true,
                "/pipeline/apis/v1beta1/experiments"),
        GET_RUNS("get_runs", HttpMethod.GET, true,
                "/pipeline/apis/v1beta1/runs"),
        GET_RUN_BY_ID("get_run_by_id", HttpMethod.GET, false,
                "/pipeline/apis/v1beta1/runs"),
        GET_RUN_BY_NAME("get_run_by_name", HttpMethod.GET, true,
                "/pipeline/apis/v1beta1/runs"),
        START_RUN("start_run", HttpMethod.POST, false,
                "/pipeline/apis/v1beta1/runs"),
        START_RUN_AND_MONITOR("start_run_and_monitor", HttpMethod.POST, false,
                "/pipeline/apis/v1beta1/runs"),
        CREATE_EXPERIMENT("create_experiment", HttpMethod.POST, false,
            "/pipeline/apis/v1beta1/experiments");

        private final String value;
        private final HttpMethod httpMethod;
        private final boolean requiresMultiuserFilter;
        private final String apiUrl;

        KubeflowApiOperationsEnum(String value, HttpMethod httpMethod, boolean requiresMultiuserFilter, String apiUrl) {
            this.value = value;
            this.httpMethod = httpMethod;
            this.requiresMultiuserFilter = requiresMultiuserFilter;
            this.apiUrl = apiUrl;
        }

        public String getValue() {
            return value;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        public boolean requiresMultiuserFilter() {
            return requiresMultiuserFilter;
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