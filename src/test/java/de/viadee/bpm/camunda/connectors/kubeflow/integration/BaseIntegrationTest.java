package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
import de.viadee.bpm.camunda.connectors.kubeflow.services.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.integration.util.KubeflowLogin;

public class BaseIntegrationTest {

  private static final String KUBEFLOW_HOST = "KUBEFLOW_HOST";
  private static final String DEFAULT_KUBEFLOW_HOST = "localhost";

  private static final String KUBEFLOW_NAMESPACE_ENV_KEY = "KUBEFLOW_NAMESPACE";
  private static final String DEFAULT_KUBEFLOW_NAMESPACE = "kubeflow-user-example-com";

  private static final String KUBEFLOW_USERNAME_ENV_KEY = "KUBEFLOW_USERNAME";
  private static final String DEFAULT_KUBEFLOW_USERNAME = "user@example.com";

  private static final String KUBEFLOW_PASSWORD_ENV_KEY = "KUBEFLOW_PASSWORD";
  private static final String DEFAULT_KUBEFLOW_PASSWORD = "12341234";

  private Configuration configuration;

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

  private String getKubeflowUrl() throws Exception {
    ProcessBuilder processBuilder = new ProcessBuilder("kubectl", "-n", "istio-system", "get", "svc",
        "istio-ingressgateway", "-ojsonpath={.spec.ports[?(@.name == 'http2')].nodePort}");
    Process process = processBuilder.start();

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    int exitVal = process.waitFor();
    if (exitVal != 0) {
      throw new IOException("Failed to get kubeflow url");
    }

    String host = getEnvOrDefault(KUBEFLOW_HOST, DEFAULT_KUBEFLOW_HOST);
    return "http://" + host + ":" + output.toString().trim();
  }

  private String getEnvOrDefault(String env, String defaultValue) {
    String value = System.getenv(env);
    return value == null ? defaultValue : value;
  }

  protected Configuration getConfiguration() {
    return configuration;
  }

  protected String getCookie() throws IOException, URISyntaxException, InterruptedException {
    String username = getEnvOrDefault(KUBEFLOW_USERNAME_ENV_KEY, DEFAULT_KUBEFLOW_USERNAME);
    String password = getEnvOrDefault(KUBEFLOW_PASSWORD_ENV_KEY, DEFAULT_KUBEFLOW_PASSWORD);
    String cookie = KubeflowLogin.getIstioAuthSession(this.getConfiguration().kubeflowUrl(), username, password);

    return cookie;
  }

  protected KubeflowConnectorExecutor getExecutor(String pipelineVersion, String operation, String experimentName)
      throws IOException, URISyntaxException, InterruptedException {
    var httpHeaders = Map.of("Cookie", this.getCookie());
    KubeflowApi kubeflowApi = new KubeflowApi(pipelineVersion, operation, null, null,
        null, null, null, null, null, experimentName, null, httpHeaders);
    KubeflowConnectorRequest kubeflowConnectorRequest = new KubeflowConnectorRequest(
        new NoAuthentication(), // Authentication via Headers
        this.getConfiguration(),
        kubeflowApi,
        new Timeout(20));
    return ExecutionHandler.getExecutor(kubeflowConnectorRequest, 0);
  }
}