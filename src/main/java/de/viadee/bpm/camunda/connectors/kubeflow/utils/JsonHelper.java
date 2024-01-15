/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.viadee.bpm.camunda.connectors.kubeflow.utils;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.camunda.connector.feel.jackson.JacksonModuleFeelFunction;

public class JsonHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonHelper.class);

  public static JsonNode getAsJsonElement(final String strResponse, final ObjectMapper mapper) {
    return Optional.ofNullable(strResponse)
        .filter(response -> !response.isBlank())
        .map(
            response -> {
              try {
                return mapper.readTree(response);
              } catch (JsonProcessingException e) {
                LOGGER.error("Wasn't able to create a JSON node from string: " + strResponse);
                throw new RuntimeException(e);
              }
            })
        .orElse(null);
  }

  public static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModules(new JacksonModuleFeelFunction(), new Jdk8Module(), new JavaTimeModule())
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
      .build();
}
