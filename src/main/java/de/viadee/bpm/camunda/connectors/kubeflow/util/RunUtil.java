package de.viadee.bpm.camunda.connectors.kubeflow.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.swagger.client.model.V1ApiListRunsResponse;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V1ApiRunDetail;
import io.swagger.client.model.V2beta1ListRunsResponse;
import io.swagger.client.model.V2beta1Run;
import org.threeten.bp.OffsetDateTime;

public class RunUtil {

  private ObjectMapper runMapper;

  public RunUtil() {
    runMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class,
            new OffsetDateTimeDeserializer()))
        .setPropertyNamingStrategy(SnakeCaseStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
  }

  public V1ApiListRunsResponse readV1RunListAsTypedResponse(HttpCommonResult runResponse)
      throws JsonProcessingException {
    var v1ApiListRunsResponse = runMapper
        .readValue(runMapper.writeValueAsString(runResponse.getBody()), V1ApiListRunsResponse.class);
    return v1ApiListRunsResponse;
  }

  public V1ApiRun readV1RunAsTypedResponse(HttpCommonResult runResponse)
      throws JsonProcessingException {
    var v1ApiRunResponse = runMapper
        .readValue(runMapper.writeValueAsString(runResponse.getBody()), V1ApiRunDetail.class).getRun();
    return v1ApiRunResponse;
  }

  public String extractIdFromV1RunResponse(HttpCommonResult runResponse) throws JsonProcessingException {
    var v1ApiRun = readV1RunAsTypedResponse(runResponse);
    return v1ApiRun == null ? null : v1ApiRun.getId();
  }

  public V2beta1ListRunsResponse readV2RunListAsTypedResponse(HttpCommonResult runResponse)
      throws JsonProcessingException {
    var v2ApiListRunsResponse = runMapper
        .readValue(runMapper.writeValueAsString(runResponse.getBody()), V2beta1ListRunsResponse.class);
    return v2ApiListRunsResponse;
  }

  public V2beta1Run readV2RunAsTypedResponse(HttpCommonResult runResponse)
      throws JsonProcessingException {
    var v2ApiRunResponse = runMapper
        .readValue(runMapper.writeValueAsString(runResponse.getBody()), V2beta1Run.class);
    return v2ApiRunResponse;
  }

  public String extractIdFromV2RunResponse(HttpCommonResult runResponse) throws JsonProcessingException {
    var v2beta1Run = readV2RunAsTypedResponse(runResponse);
    return v2beta1Run == null ? null : v2beta1Run.getRunId();
  }

}
