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
package de.viadee.bpm.camunda.connectors.kubeflow.auth;

import java.util.Map;
import java.util.function.Function;

import io.camunda.connector.api.annotation.FEEL;
import io.camunda.connector.generator.annotation.TemplateProperty;
import io.camunda.connector.generator.annotation.TemplateSubType;
import jakarta.validation.constraints.NotEmpty;

@TemplateSubType(id = BasicAuthentication.TYPE, label = "Basic")
public final class BasicAuthentication extends Authentication {
  @TemplateProperty(ignore = true)
  private static final String SPEC_PASSWORD_EMPTY_PATTERN = "SPEC_PASSWORD_EMPTY_PATTERN";

  @TemplateProperty(ignore = true)
  private static final Function<String, String> SPEC_PASSWORD =
      (psw) -> psw.equals(SPEC_PASSWORD_EMPTY_PATTERN) ? "" : psw;

  @FEEL
  @NotEmpty
  @TemplateProperty(group = "authentication")
  private String username;

  @FEEL
  @NotEmpty
  @TemplateProperty(group = "authentication")
  private String password;

  @Override
  public void setHeaders(final Map<String, String> headers) {
    headers.put(username, SPEC_PASSWORD.apply(password));
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "BasicAuthentication {"
        + "username='[REDACTED]'"
        + ", password='[REDACTED]'"
        + "}; Super: "
        + super.toString();
  }

  @TemplateProperty(ignore = true)
  public static final String TYPE = "basic";
}
