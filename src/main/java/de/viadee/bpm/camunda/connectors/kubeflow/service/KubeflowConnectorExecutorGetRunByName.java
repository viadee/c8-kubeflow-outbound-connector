package de.viadee.bpm.camunda.connectors.kubeflow.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;
import java.io.IOException;
import org.apache.commons.lang3.tuple.Pair;

public class KubeflowConnectorExecutorGetRunByName extends KubeflowConnectorExecutor {

  private static final String HTTP_BODY_TOTAL_SIZE = "total_size";
  private static final Pair<String, String> FILTER_BY_OPERATION_PAIR = Pair.of("op", "EQUALS");
  private static final Pair<String, String> FILTER_BY_KEY_PAIR = Pair.of("key", "name");
  private static final String FILTER_BY_RUN_NAME_KEY = "string_value";

  public KubeflowConnectorExecutorGetRunByName(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
    super(connectorRequest, processInstanceKey, kubeflowApiOperationsEnum);
  }

  @Override
  protected String getFilterString() {
    var filter = objectMapper.createObjectNode();
    filter.put("predicates", buildArrayNode());
    return filter.toString();
  }

  @Override
  public HttpCommonResult execute(HttpService httpService)
      throws InstantiationException, IllegalAccessException, IOException {
    var runByNameHttpResult = super.execute(httpService);
    var runByNameStringResult = objectMapper.writeValueAsString(runByNameHttpResult.getBody());
    var runByNameJsonResult = objectMapper.readTree(runByNameStringResult);

    var numberOfRunsFoundByName = runByNameJsonResult.get(HTTP_BODY_TOTAL_SIZE) == null
        ? 0 : runByNameJsonResult.get(HTTP_BODY_TOTAL_SIZE).intValue();

    if (numberOfRunsFoundByName > 1) {
      throw new RuntimeException("Could not uniquely identify run by name");
    }

    return runByNameHttpResult;
  }

  private ArrayNode buildArrayNode() {
    var jsonNode = objectMapper.createObjectNode();
    jsonNode.put(FILTER_BY_OPERATION_PAIR.getKey(), FILTER_BY_OPERATION_PAIR.getValue());
    jsonNode.put(FILTER_BY_KEY_PAIR.getKey(), FILTER_BY_KEY_PAIR.getValue());
    jsonNode.put(FILTER_BY_RUN_NAME_KEY, connectorRequest.kubeflowapi().filter());
    return objectMapper.createArrayNode().add(jsonNode);
  }
}
