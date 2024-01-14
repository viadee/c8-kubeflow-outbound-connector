package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.V1Filter;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.V1FilterPredicate;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.RunUtil;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Filter;
import io.swagger.client.model.V2beta1Predicate;
import io.swagger.client.model.V2beta1PredicateOperation;
import io.swagger.client.model.V2beta1Run;

public class KubeflowConnectorExecutorGetRunByName extends KubeflowConnectorExecutor {
  private static final String FILTER_BY_KEY = "name";
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private RunUtil runUtil;

  public KubeflowConnectorExecutorGetRunByName(KubeflowConnectorRequest connectorRequest, long processInstanceKey,
      KubeflowApisEnum kubeflowApisEnum,
      KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
    super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    this.runUtil = new RunUtil();
  }

  @Override
  protected String getFilterString() {
    var filter = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ? getV1Filter() : getV2Filter();
    try {
      return objectMapper.writeValueAsString(filter);
    } catch (Exception e) {
      throw new RuntimeException("Error occurred during serialization of filter object");
    }
  }

  @Override
  public HttpResponse<String> execute(HttpClient httpClient) {
    var runByNameHttpResult = super.execute(httpClient);

    Integer apiListRunsResponseSize = null;
    try {
      apiListRunsResponseSize = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)
          ? runUtil.readV1RunListAsTypedResponse(runByNameHttpResult).getTotalSize()
          : runUtil.readV2RunListAsTypedResponse(runByNameHttpResult).getTotalSize();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    if (apiListRunsResponseSize == null || apiListRunsResponseSize <= 1) {
      return runByNameHttpResult;
    }

    throw new RuntimeException("Could not uniquely identify run by name");

  }

  public Optional<V1ApiRun> getRunByNameV1Typed(HttpClient httpClient)
      throws IOException, InstantiationException, IllegalAccessException {
    var httpResponse = this.execute(httpClient);
    var v1RunList = runUtil.readV1RunListAsTypedResponse(httpResponse).getRuns();
    var v1ApiRun = v1RunList == null || v1RunList.isEmpty() ? null : v1RunList.get(0);
    return Optional.ofNullable(v1ApiRun);
  }

  public Optional<V2beta1Run> getRunByNameV2Typed(HttpClient httpClient)
      throws IOException, InstantiationException, IllegalAccessException {
    var httpResponse = this.execute(httpClient);
    var v2RunList = runUtil.readV2RunListAsTypedResponse(httpResponse).getRuns();
    var v2ApiRun = v2RunList == null || v2RunList.isEmpty() ? null : v2RunList.get(0);
    return Optional.ofNullable(v2ApiRun);
  }

  private V1Filter getV1Filter() {
    var predicate = new V1FilterPredicate()
        .op(V2beta1PredicateOperation.EQUALS)
        .key(FILTER_BY_KEY)
        .stringValue(connectorRequest.kubeflowapi().filter());

    return new V1Filter()
        .addPredicatesItem(predicate);
  }

  private V2beta1Filter getV2Filter() {
    var predicate = new V2beta1Predicate()
        .operation(V2beta1PredicateOperation.EQUALS)
        .key(FILTER_BY_KEY)
        .stringValue(connectorRequest.kubeflowapi().filter());

    return new V2beta1Filter()
        .addPredicatesItem(predicate);
  }
}
