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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EnvironmentAuthentication.class, name = EnvironmentAuthentication.TYPE),
  @JsonSubTypes.Type(value = BasicAuthentication.class, name = BasicAuthentication.TYPE),
  @JsonSubTypes.Type(value = NoAuthentication.class, name = NoAuthentication.TYPE),
  @JsonSubTypes.Type(value = OAuthAuthenticationClientCredentialsFlow.class, name = OAuthAuthenticationClientCredentialsFlow.TYPE),
  @JsonSubTypes.Type(value = OAuthAuthenticationPasswordFlow.class, name = OAuthAuthenticationPasswordFlow.TYPE),
  @JsonSubTypes.Type(value = BearerAuthentication.class, name = BearerAuthentication.TYPE)
})
public abstract sealed class Authentication
    permits EnvironmentAuthentication,
        BasicAuthentication,
        BearerAuthentication,
        NoAuthentication,
        OAuthAuthenticationClientCredentialsFlow,
        OAuthAuthenticationPasswordFlow {

  public abstract void setHeaders(Map<String, String> headers);
}
