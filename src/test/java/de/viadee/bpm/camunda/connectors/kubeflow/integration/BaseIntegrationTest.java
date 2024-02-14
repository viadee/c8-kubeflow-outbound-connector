package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.integration.util.KubeflowLogin;
import io.camunda.connector.http.base.services.HttpService;

public class BaseIntegrationTest {

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

  protected static final HttpService createHttpService() {
    final ObjectMapper objectMapper = new ObjectMapper();
    final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
    return new HttpService(objectMapper, requestFactory);
  }

  private static final String getEnvOrDefault(String env, String defaultValue) {
    String value = System.getenv(env);
    return value != null ? value : defaultValue;
  }

  private static String getKubeflowUrl() throws Exception {
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

    return "http://localhost:" + output.toString().trim();
  }

  private Configuration createConfiguration() throws Exception {
    String username = getEnvOrDefault(KUBEFLOW_USERNAME_ENV_KEY, DEFAULT_KUBEFLOW_USERNAME);
    String password = getEnvOrDefault(KUBEFLOW_PASSWORD_ENV_KEY, DEFAULT_KUBEFLOW_PASSWORD);

    String kubeflowUrl = getKubeflowUrl();
    String cookie = KubeflowLogin.getIstioAuthSession(kubeflowUrl, username, password);

    return new Configuration(cookie, kubeflowUrl,
        getEnvOrDefault(KUBEFLOW_NAMESPACE_ENV_KEY, DEFAULT_KUBEFLOW_NAMESPACE));
  }

  protected Configuration getConfiguration() {
    return configuration;
  }
}