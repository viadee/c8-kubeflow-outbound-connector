package de.viadee.bpm.camunda.connectors.kubeflow.unit.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.JsonHelper;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonHelperTest {

  JsonHelper jsonHelper = new JsonHelper();
  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void getAsJsonElement() {
    // given
    var input = "{\"id\":\"1\",\"name\":\"test\"}";
    // when
    var result = jsonHelper.getAsJsonElement(input, objectMapper);
    // then
    assertEquals(2, result.size());
    assertTrue(result.has("id"));
    assertTrue(result.has("name"));
    assertFalse(result.has("1"));
    assertFalse(result.has("test"));
    assertEquals("1", result.get("id").textValue());
    assertEquals("test", result.get("name").textValue());
  }
}