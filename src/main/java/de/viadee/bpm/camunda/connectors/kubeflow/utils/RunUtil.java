package de.viadee.bpm.camunda.connectors.kubeflow.utils;

import java.net.http.HttpResponse;

import org.threeten.bp.OffsetDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.swagger.client.model.V1ApiListRunsResponse;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V1ApiRunDetail;
import io.swagger.client.model.V2beta1ListRunsResponse;
import io.swagger.client.model.V2beta1Run;

public class RunUtil {

  private static final ObjectMapper runMapper = new ObjectMapper()
      .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class,
          new OffsetDateTimeDeserializer()))
      .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

  public RunUtil() {
  }

  public V1ApiListRunsResponse readV1RunListAsTypedResponse(HttpResponse<String> runResponse)
      throws JsonProcessingException {
    var v1ApiListRunsResponse = runMapper
        .readValue(runResponse.body(), V1ApiListRunsResponse.class);
    return v1ApiListRunsResponse;
  }

  public V1ApiRun readV1RunAsTypedResponse(HttpResponse<String> runResponse)
      throws JsonProcessingException {
    V1ApiRun v1ApiRunResponse = null;
    if (!JsonHelper.getAsJsonElement(runResponse.body(), new ObjectMapper()).isEmpty()) {
      v1ApiRunResponse = runMapper
          .readValue(runResponse.body(), V1ApiRunDetail.class).getRun();
    }
    return v1ApiRunResponse;
  }

  public String extractIdFromV1RunResponse(HttpResponse<String> runResponse) throws JsonProcessingException {
    var v1ApiRun = readV1RunAsTypedResponse(runResponse);
    return v1ApiRun == null ? null : v1ApiRun.getId();
  }

  public V2beta1ListRunsResponse readV2RunListAsTypedResponse(HttpResponse<String> runResponse)
      throws JsonProcessingException {
    var v2ApiListRunsResponse = runMapper
        .readValue(runResponse.body(), V2beta1ListRunsResponse.class);
    return v2ApiListRunsResponse;
  }

  public V2beta1Run readV2RunAsTypedResponse(HttpResponse<String> runResponse)
      throws JsonProcessingException {
    var v2ApiRunResponse = runMapper
        .readValue(runResponse.body(), V2beta1Run.class);
    return v2ApiRunResponse;
  }

  public String extractIdFromV2RunResponse(HttpResponse<String> runResponse) throws JsonProcessingException {
    var v2beta1Run = readV2RunAsTypedResponse(runResponse);
    return v2beta1Run == null ? null : v2beta1Run.getRunId();
  }

}
