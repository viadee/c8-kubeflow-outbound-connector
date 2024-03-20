package de.viadee.bpm.camunda.connectors.kubeflow.enums;

import java.util.Arrays;

public enum TypeOfUserModeEnum {
  MULTI_USER_MODE("multiUserMode");

  private final String value;

  TypeOfUserModeEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TypeOfUserModeEnum fromValue(String value) {
    return Arrays
        .stream(values())
        .filter(typeOfUserModeEnum -> typeOfUserModeEnum.value.equals(value))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unbekannter Wert: " + value)
        );
  }
}
