package de.viadee.bpm.camunda.connectors.kubeflow.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.BearerAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.EnvironmentAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationClientCredentialsFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthUtilTest {

  KubeflowApi kubeflowApiMock;

  @BeforeEach
  public void setup() {
    kubeflowApiMock = mock(KubeflowApi.class);
  }

  @Test
  void setAuthenticationParameters() {
    // given
    var kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new EnvironmentAuthentication(),
        new Configuration("http://localhost:8080", "multiUserMode"),
        kubeflowApiMock,
        new Timeout(20)
    );
    // when
    AuthUtil.setAuthenticationParameters(kubeflowConnectorRequest);
    // then (read expected values from env. vars that are set in pom.xml)
    assertTrue(kubeflowConnectorRequest.getAuthentication() instanceof OAuthAuthenticationClientCredentialsFlow);
    assertEquals("https//idProviderEndpoint.de", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getOauthTokenEndpoint());
    assertEquals("1", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getClientId());
    assertEquals("##23#43#87##23#", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getClientSecretCC());
    assertEquals("testScope", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getScopes());
    assertEquals("testAudience", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getAudience());
    assertEquals("testOAuthClient", ((OAuthAuthenticationClientCredentialsFlow) kubeflowConnectorRequest.getAuthentication()).getClientAuthentication());
  }

}