package de.viadee.bpm.camunda.connectors.kubeflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.util.RunUtil;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Run;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class KubeflowConnectorExecutorGetRunByName extends KubeflowConnectorExecutor {

  private static final Pair<String, String> FILTER_BY_OPERATION_PAIR_V1 = Pair.of("op", "EQUALS");
  private static final Pair<String, String> FILTER_BY_OPERATION_PAIR_V2 = Pair.of("operation", "EQUALS");

  private static final Pair<String, String> FILTER_BY_KEY_PAIR = Pair.of("key", "name");
  private static final String FILTER_BY_RUN_NAME_KEY = "string_value";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private RunUtil runUtil;

  public KubeflowConnectorExecutorGetRunByName(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
      KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
    super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    this.runUtil = new RunUtil();
  }

  @Override
  protected String getFilterString() { // TODO pojo
    var filter = objectMapper.createObjectNode();
    filter.put("predicates", buildArrayNode());
    return filter.toString();
  }

  @Override
  public HttpCommonResult execute(HttpService httpService)
      throws InstantiationException, IllegalAccessException, IOException {
    var runByNameHttpResult = super.execute(httpService);

    var apiListRunsResponseSize = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
        runUtil.readV1RunListAsTypedResponse(runByNameHttpResult).getTotalSize() :
        runUtil.readV2RunListAsTypedResponse(runByNameHttpResult).getTotalSize();

    if (apiListRunsResponseSize == null || apiListRunsResponseSize <= 1) {
      return runByNameHttpResult;
    }

    throw new RuntimeException("Could not uniquely identify run by name");

  }

  public Optional<V1ApiRun> getRunByNameV1Typed(HttpService httpService)
      throws IOException, InstantiationException, IllegalAccessException {
    var httpCommonResult = this.execute(httpService);
    var v1RunList = runUtil.readV1RunListAsTypedResponse(httpCommonResult).getRuns();
    var v1ApiRun = v1RunList == null || v1RunList.isEmpty() ? null : v1RunList.get(0);
    return Optional.ofNullable(v1ApiRun);
  }

  public Optional<V2beta1Run> getRunByNameV2Typed(HttpService httpService)
      throws IOException, InstantiationException, IllegalAccessException {
    var httpCommonResult = this.execute(httpService);
    var v2RunList = runUtil.readV2RunListAsTypedResponse(httpCommonResult).getRuns();
    var v2ApiRun = v2RunList == null || v2RunList.isEmpty() ? null : v2RunList.get(0);
    return Optional.ofNullable(v2ApiRun);
  }

  private ArrayNode buildArrayNode() { // TODO pojo
    var jsonNode = objectMapper.createObjectNode();
    var filterByOperationPair = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
        FILTER_BY_OPERATION_PAIR_V1 : FILTER_BY_OPERATION_PAIR_V2;
    jsonNode.put(filterByOperationPair.getKey(), filterByOperationPair.getValue());
    jsonNode.put(FILTER_BY_KEY_PAIR.getKey(), FILTER_BY_KEY_PAIR.getValue());
    jsonNode.put(FILTER_BY_RUN_NAME_KEY, connectorRequest.kubeflowapi().filter());
    return objectMapper.createArrayNode().add(jsonNode);
  }
}
