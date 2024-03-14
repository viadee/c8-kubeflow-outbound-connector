package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationClientCredentialsFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.OffsetDateTimeDeserializer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import org.threeten.bp.OffsetDateTime;

public class BaseIntegrationTest {

  private static final String KUBEFLOW_HOST = "KUBEFLOW_HOST";
  private static final String DEFAULT_KUBEFLOW_HOST = "localhost";

  private static final String KUBEFLOW_NAMESPACE_ENV_KEY = "KUBEFLOW_NAMESPACE";
  private static final String DEFAULT_KUBEFLOW_NAMESPACE = "kubeflow-service-example-com";

  private static final String KUBEFLOW_USERNAME_ENV_KEY = "KUBEFLOW_USERNAME";
  private static final String DEFAULT_KUBEFLOW_USERNAME = "user@example.com";

  private static final String KUBEFLOW_PASSWORD_ENV_KEY = "KUBEFLOW_PASSWORD";
  private static final String DEFAULT_KUBEFLOW_PASSWORD = "12341234";

  private static final String KUBEFLOW_CLIENT_ID_ENV_KEY = "KUBEFLOW_CLIENT_ID";
  private static final String DEFAULT_KUBEFLOW_CLIENT_ID = "kubeflow";

  private static final String KUBEFLOW_CLIENT_SECRET_ENV_KEY = "KUBEFLOW_CLIENT_SECRET";
  private static final String DEFAULT_CLIENT_SECRET = "Jq09L1liFa0UiaXnL3pcnXzlqOKXaoOW";

  private Configuration configuration;
  private OAuthAuthenticationClientCredentialsFlow oAuthAuthenticationClientCredentialsFlow;

  protected static final Integer PROCESS_INSTANCE_ID = 100;

  protected static ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new SimpleModule().addDeserializer(OffsetDateTime.class,
          new OffsetDateTimeDeserializer()))
      .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

  @BeforeEach
  protected void setUp() throws Exception {
    if (configuration == null) {
      configuration = createConfiguration();
    }
  }

  private Configuration createConfiguration() throws Exception {
    String kubeflowUrl = getKubeflowUrl();

    return new Configuration(kubeflowUrl,
        getEnvOrDefault(KUBEFLOW_NAMESPACE_ENV_KEY, DEFAULT_KUBEFLOW_NAMESPACE));
  }

  private OAuthAuthenticationClientCredentialsFlow createOAuthAuthenticationClientCredentialsFlow() throws Exception {
    String kubeflowUrl = getKubeflowUrl();

    OAuthAuthenticationClientCredentialsFlow oAuthAuthenticationClientCredentialsFlow = new OAuthAuthenticationClientCredentialsFlow();
    oAuthAuthenticationClientCredentialsFlow.setAudience("kubeflow");
    oAuthAuthenticationClientCredentialsFlow.setClientAuthentication("bearer");
    oAuthAuthenticationClientCredentialsFlow.setClientId("kubeflow");
    oAuthAuthenticationClientCredentialsFlow.setClientSecretCC("Jq09L1liFa0UiaXnL3pcnXzlqOKXaoOW");
    oAuthAuthenticationClientCredentialsFlow.setOauthTokenEndpoint(kubeflowUrl+"/auth/realms/kubeflow/protocol/openid-connect/token");
    oAuthAuthenticationClientCredentialsFlow.setScopes("profile email openid groups");

    return oAuthAuthenticationClientCredentialsFlow;
  }

  private String getKubeflowUrl() throws Exception {
    String host = getEnvOrDefault(KUBEFLOW_HOST, DEFAULT_KUBEFLOW_HOST);
    return "http://" + host + ":30001";
  }

  private String getEnvOrDefault(String env, String defaultValue) {
    String value = System.getenv(env);
    return value == null ? defaultValue : value;
  }

  protected Configuration getConfiguration() {
    return configuration;
  }

  protected KubeflowConnectorExecutor getExecutor(String pipelineVersion, String operation, String experimentName,
      String pipelineId, String experimentId, String runName)
      throws Exception {
    KubeflowApi kubeflowApi = new KubeflowApi(pipelineVersion, operation, null, runName,
        null, pipelineId, experimentId, null, null, experimentName, null, null);
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(
        this.createOAuthAuthenticationClientCredentialsFlow(), // Authentication via OAuth
        this.getConfiguration(),
        kubeflowApi,
        new Timeout(20));
    return ExecutionHandler.getExecutor(kubeflowConnectorRequest, PROCESS_INSTANCE_ID);
  }

  protected HttpResponse<String> getPipelines(String pipelineVersion) throws Exception {
    return getExecutor(pipelineVersion, "get_pipelines", null,
        null, null, null).execute();
  }
}