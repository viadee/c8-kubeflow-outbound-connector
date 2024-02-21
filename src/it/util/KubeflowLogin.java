package de.viadee.bpm.camunda.connectors.kubeflow.integration.util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class KubeflowLogin {

    public static String getIstioAuthSession(String url, String username, String password)
            throws IOException, InterruptedException, URISyntaxException {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        // Use HttpClient for making requests
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(CookieHandler.getDefault())
                .build();

        // Define the GET request
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

        // Send the GET request and handle the response
        HttpResponse<String> getResponse;
        try {
            getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send GET request to: " + url, e);
        }
        if (getResponse.statusCode() != 200) {
            throw new RuntimeException("HTTP status code '" + getResponse.statusCode() + "' for GET against: " + url);
        }

        // Extract the redirect URL
        String redirectUrl = getResponse.uri().toString();

        // Extract Dex Login URL
        if (!Pattern.compile("/auth/.*/login").matcher(redirectUrl).find()) {
            throw new RuntimeException("No redirect to Dex login page found in response from: " + url);
        }
        String dexLoginUrl = redirectUrl;

        // Define the POST request for login
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI(dexLoginUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("login=" + username + "&password=" + password))
                .build();

        // Send the POST request and handle the response
        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        if (postResponse.statusCode() != 200) {
            throw new RuntimeException(
                    "Login credentials were probably invalid - No redirect after POST to: " + dexLoginUrl);
        }

        // Extract session cookie from CookieManager
        for (var cookie : cookieManager.getCookieStore().get(new URI(url))) {
            if (cookie.getName().equals("authservice_session")) {
                return cookie.getName() + "=" + cookie.getValue();
            }
        }

        throw new RuntimeException("No session cookie found in response from: " + url);
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        String sessionCookie = getIstioAuthSession("http://localhost:31641", "user@example.com", "12341234");
        System.out.println(sessionCookie);
    }
}
