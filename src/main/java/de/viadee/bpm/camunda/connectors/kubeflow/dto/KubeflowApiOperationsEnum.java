package de.viadee.bpm.camunda.connectors.kubeflow.dto;

public enum KubeflowApiOperationsEnum {
        KubeflowApiOperationsEnum("get_pipelines",
                "/pipeline/apis/v1beta1/runs");

        private final String value;
        private final String apiUrl;

        KubeflowApiOperationsEnum(String value, String apiUrl) {
            this.value = value;
            this.apiUrl = apiUrl;
        }

        public String getValue() {
            return value;
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