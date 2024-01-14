package de.viadee.bpm.camunda.connectors.kubeflow.entities;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class V1Filter {
  @SerializedName("predicates")
  private List<V1FilterPredicate> predicates = null;

  public V1Filter predicates(List<V1FilterPredicate> predicates) {
    this.predicates = predicates;
    return this;
  }

  public V1Filter addPredicatesItem(V1FilterPredicate predicatesItem) {
    if (this.predicates == null) {
      this.predicates = new ArrayList<V1FilterPredicate>();
    }
    this.predicates.add(predicatesItem);
    return this;
  }

  public List<V1FilterPredicate> getPredicates() {
    return predicates;
  }

  public void setPredicates(
      List<V1FilterPredicate> predicates) {
    this.predicates = predicates;
  }
}
