package de.viadee.bpm.camunda.connectors.kubeflow.integration;

import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;

public class DexLoginIT extends BaseIntegrationTest {

  @Test
  public void testLogin() throws Exception {
    Configuration configuration = this.getConfiguration();
    // Run a plain request against the url to check the response code
    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
        .uri(new URL(configuration.kubeflowUrl()).toURI())
        .header("Cookie", configuration.cookievalue())
        .GET()
        .build();

    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    java.net.http.HttpResponse<String> response = client.send(request,
        java.net.http.HttpResponse.BodyHandlers.ofString());

    // Assert response code is 200
    Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
  }
}