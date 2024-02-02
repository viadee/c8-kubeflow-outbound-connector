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

import io.camunda.connector.generator.annotation.TemplateProperty;
import io.camunda.connector.generator.annotation.TemplateSubType;

@TemplateSubType(id = EnvironmentAuthentication.TYPE, label = "Environment")
public final class EnvironmentAuthentication extends Authentication {

  @Override
  public String toString() {
    return "EnvironmentAuthentication {"
        + "}; Super: "
        + super.toString();
  }

  @TemplateProperty(ignore = true)
  public static final String TYPE = "environment";

  @Override
  public void setHeaders(Map<String, String> headers) {
    // do nothing
  }
}
