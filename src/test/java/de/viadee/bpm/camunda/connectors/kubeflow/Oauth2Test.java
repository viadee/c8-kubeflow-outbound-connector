package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Oauth2Test {

    public static void main(String[] args) throws IOException {

        // HttpRequest request;
        // try {
        // request = new HttpRequestBuilder().genericUrl(new
        // GenericUrl("https://kubeflow.l.micudaj.de/pipeline")).method(HttpMethod.GET).build(requestFactory);
        // HttpResponse response = request.execute();
        // } catch (HttpResponseException e) {
        // if(e.getStatusCode() == 302) {
        // String location = e.getHeaders().getLocation();

        // try {
        // request = new HttpRequestBuilder().genericUrl(new
        // GenericUrl(location)).method(HttpMethod.GET).build(requestFactory);
        // HttpResponse response = request.execute();
        // String html = new BufferedReader(
        // new InputStreamReader(response.getContent(), StandardCharsets.UTF_8))
        // .lines()
        // .collect(Collectors.joining("\n"));

        // // Parse the HTML string
        // Document doc = Jsoup.parse(html);

        // // Find the form with the specific ID
        // Element loginForm = doc.selectFirst("form#kc-form-login");

        // // Extract the action attribute
        // if (loginForm != null) {
        // String loginUrl = loginForm.attr("action");

        // //Login to Idp
        // HttpContent
        // request = new HttpRequestBuilder()
        // .genericUrl(new GenericUrl(loginUrl))
        // .method(HttpMethod.POST)
        // .content(n)
        // .build(requestFactory);
        // } else {
        // System.out.println("Login form not found.");
        // }
        // String a = "";
        // } catch (IOException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // }
        // }catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // HttpCommonRequest firstRequest = new HttpCommonRequest();
        // firstRequest.setMethod(HttpMethod.GET);
        // firstRequest.setUrl("https://kubeflow.l.micudaj.de/pipeline");

        HttpClient httpClient = HttpClient.newHttpClient();
        String serviceUrl = "https://id.micudaj.de/auth/realms/micudaj/protocol/openid-connect/token";
        Map<Object, Object> data = new HashMap<>();
        data.put("username", "user@example.com");
        data.put("password", "12341234");
        data.put("grant_type", "password");
        data.put("client_id", "kubeflow-password");
        data.put("scope", "email profile openid roles");

        HttpRequest request = HttpRequest.newBuilder()
                .method("POST", ofFormData(data))
                .uri(URI.create(serviceUrl))
                .setHeader("User-Agent", "Kubeflow Camunda Connector")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // print status code
            System.out.println(response.statusCode());

            // print response body
            System.out.println(response.body());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // HttpCommonRequest oauthRequest = new HttpCommonRequest();
        // oauthRequest.setMethod(HttpMethod.POST);
        // oauthRequest.setBody(Map.of("username", "user@example.com", "password",
        // "12341234", "grant_type", "password",
        // "client_id", "kubeflow-password", "scope", "email profile openid roles"));
        // oauthRequest.setHeaders(Map.of("Content-Type",
        // Constants.APPLICATION_X_WWW_FORM_URLENCODED));
        // oauthRequest.setUrl("https://id.micudaj.de/auth/realms/micudaj/protocol/openid-connect/token");

        // HttpCommonResult result;
        // String accesstoken = "";
        // String idtoken = "";
        // try {
        // result = httpService.executeConnectorRequest(oauthRequest);
        // Map map = (Map) result.getBody();
        // accesstoken = (String) map.get("access_token");
        // idtoken = (String) map.get("id_token");
        // } catch (InstantiationException | IllegalAccessException | IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // HttpCommonRequest testRequest = new HttpCommonRequest();
        // testRequest.setMethod(HttpMethod.GET);
        // testRequest.setUrl("http://k8s.micudaj.de/pipeline/apis/v1beta1/pipelines");

        // BearerAuthentication bearerAuth = new BearerAuthentication();
        // bearerAuth.setToken(accesstoken);
        // testRequest.setAuthentication(bearerAuth);

        // try {
        // result = httpService.executeConnectorRequest(testRequest);

        // String a = "";
        // } catch (InstantiationException | IllegalAccessException | IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
