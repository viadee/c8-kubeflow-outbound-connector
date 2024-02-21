package de.viadee.bpm.camunda.connectors.kubeflow.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RunUtilTest {
  RunUtil runUtil = new RunUtil();
  HttpResponse<String> httpResponseMock;

  @BeforeEach
  public void setup() {
    httpResponseMock = mock(HttpResponse.class);
  }

  @Test
  void readV1RunListAsTypedResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"total_size\":\"0\"}");
    // when
    var result = runUtil.readV1RunListAsTypedResponse(httpResponseMock);
    // then
    assertEquals(0, result.getTotalSize());
    assertEquals(null, result.getRuns());
  }

  @Test
  void readV1RunAsTypedResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"id\":\"1\",\"name\":\"test\"}");
    // when
    var result = runUtil.readV1RunAsTypedResponse(httpResponseMock);
    // then
    assertEquals("1", result.getId());
    assertEquals("test", result.getName());
    assertEquals(null, result.getDescription());
  }

  @Test
  void extractIdFromV1RunResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"id\":\"1\",\"name\":\"test\"}");
    // when
    var result = runUtil.extractIdFromV1RunResponse(httpResponseMock);
    // then
    assertEquals("1", result);
  }

  @Test
  void readV2RunListAsTypedResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"total_size\":\"0\"}");
    // when
    var result = runUtil.readV2RunListAsTypedResponse(httpResponseMock);
    // then
    assertEquals(0, result.getTotalSize());
    assertEquals(null, result.getRuns());
  }

  @Test
  void readV2RunAsTypedResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"run_id\":\"1\",\"display_name\":\"test\"}");
    // when
    var result = runUtil.readV2RunAsTypedResponse(httpResponseMock);
    // then
    assertEquals("1", result.getRunId());
    assertEquals("test", result.getDisplayName());
    assertEquals(null, result.getDescription());
  }

  @Test
  void extractIdFromV2RunResponse() throws JsonProcessingException {
    // given
    when(httpResponseMock.body()).thenReturn("{\"run_id\":\"1\",\"display_name\":\"test\"}");
    // when
    var result = runUtil.extractIdFromV2RunResponse(httpResponseMock);
    // then
    assertEquals("1", result);
  }

  @Test
  void convertToV1ApiParameterList() {
    // given
    var input = Map.of("name1", (Object) "value1");
    // when
    var result = runUtil.convertToV1ApiParameterList(input);
    // then
    assertEquals(1, result.size());
    assertEquals("name1", result.get(0).getName());
    assertEquals("value1", result.get(0).getValue());
  }
}