package de.viadee.bpm.camunda.connectors.kubeflow.entities;

import com.google.gson.annotations.SerializedName;
import io.swagger.client.model.V2beta1PredicateOperation;

public class V1FilterPredicate {
  @SerializedName("op")
  private V2beta1PredicateOperation op = null;
  @SerializedName("key")
  private String key = null;
  @SerializedName("string_value")
  private String stringValue = null;

  public V1FilterPredicate op(V2beta1PredicateOperation op) {
    this.op = op;
    return this;
  }

  public V2beta1PredicateOperation getOp() {
    return op;
  }

  public void setOp(V2beta1PredicateOperation op) {
    this.op = op;
  }

  public V1FilterPredicate key(String key) {
    this.key = key;
    return this;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public V1FilterPredicate stringValue(String stringValue) {
    this.stringValue = stringValue;
    return this;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
}
