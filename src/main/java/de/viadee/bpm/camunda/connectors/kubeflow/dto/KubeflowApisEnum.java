package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import java.util.Arrays;

public enum KubeflowApisEnum {
  PIPELINES_V1("pipelinesV1", "v1beta1"),
  PIPELINES_V2("pipelinesV2", "v2beta1");

  private final String value;
  private final String urlPathVersion;

  KubeflowApisEnum(String value, String urlPathVersion) {
    this.value = value;
    this.urlPathVersion = urlPathVersion;
  }

  public static KubeflowApisEnum fromValue(String value) {
                return Arrays
                    .stream(values())
                    .filter(kubeflowApisEnum -> kubeflowApisEnum.value.equals(value))
                    .findFirst()
                    .orElseThrow(
                        () -> new IllegalArgumentException("Unbekannter Wert: " + value)
                    );
  }

  public String getValue() {
    return value;
  }

  public String getUrlPathVersion() {
    return urlPathVersion;
  }
}