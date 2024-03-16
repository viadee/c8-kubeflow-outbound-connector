package de.viadee.bpm.camunda.connectors.kubeflow.enums;

public enum TypeOfUserModeEnum {
  SINGLE_USER_MODE("singleUserMode"),
  MULTI_USER_MODE("multiUserMode");

  private final String value;

  TypeOfUserModeEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
