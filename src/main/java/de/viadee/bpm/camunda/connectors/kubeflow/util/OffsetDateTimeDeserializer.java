package de.viadee.bpm.camunda.connectors.kubeflow.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.threeten.bp.OffsetDateTime;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
  @Override
  public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return OffsetDateTime.parse(p.readValueAs(String.class));
  }
}
