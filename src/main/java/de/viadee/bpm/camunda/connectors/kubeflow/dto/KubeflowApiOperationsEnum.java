package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import io.camunda.connector.http.base.model.HttpMethod;

public enum KubeflowApiOperationsEnum {
        KubeflowApiOperationsEnum("get_pipelines", HttpMethod.GET,
                "/pipeline/apis/v1beta1/runs");

        private final String value;
        private final HttpMethod httpMethod;
        private final String apiUrl;

        KubeflowApiOperationsEnum(String value, HttpMethod httpMethod, String apiUrl) {
            this.value = value;
            this.httpMethod = httpMethod;
            this.apiUrl = apiUrl;
        }

        public String getValue() {
            return value;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public static KubeflowApiOperationsEnum fromValue(String value) {
            for (KubeflowApiOperationsEnum apiOperations : values()) {
                if (apiOperations.getValue().equals(value)) {
                    return apiOperations;
                }
            }
            throw new IllegalArgumentException("Unbekannter Wert: " + value);
        }
    }