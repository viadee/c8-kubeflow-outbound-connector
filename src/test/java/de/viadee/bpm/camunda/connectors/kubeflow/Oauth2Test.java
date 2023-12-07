package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpRequestFactory;

import io.camunda.connector.feel.ConnectorsObjectMapperSupplier;
import io.camunda.connector.http.base.auth.BearerAuthentication;
import io.camunda.connector.http.base.components.HttpTransportComponentSupplier;
import io.camunda.connector.http.base.constants.Constants;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.model.HttpMethod;
import io.camunda.connector.http.base.services.AuthenticationService;
import io.camunda.connector.http.base.services.HttpInteractionService;
import io.camunda.connector.http.base.services.HttpService;

public class Oauth2Test {

    public static void main(String[] args) throws IOException {

        ObjectMapper objectMapper = ConnectorsObjectMapperSupplier.getCopy();
        HttpRequestFactory requestFactory = HttpTransportComponentSupplier.httpRequestFactoryInstance();
        HttpInteractionService httpInteractionService = new HttpInteractionService(objectMapper);
        AuthenticationService authService = new AuthenticationService(objectMapper, requestFactory);
        HttpService httpService = new HttpService(objectMapper, requestFactory);

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

        HttpCommonRequest oauthRequest = new HttpCommonRequest();
        oauthRequest.setMethod(HttpMethod.POST);
        oauthRequest.setBody(Map.of("username", "user@example.com", "password", "12341234", "grant_type", "password",
                "client_id", "kubeflow-password", "scope", "email profile openid roles"));
        oauthRequest.setHeaders(Map.of("Content-Type", Constants.APPLICATION_X_WWW_FORM_URLENCODED));
        oauthRequest.setUrl("https://id.micudaj.de/auth/realms/micudaj/protocol/openid-connect/token");

        HttpCommonResult result;
        String accesstoken = "";
        String idtoken = "";
        try {
            result = httpService.executeConnectorRequest(oauthRequest);
            Map map = (Map) result.getBody();
            accesstoken = (String) map.get("access_token");
            idtoken = (String) map.get("id_token");
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpCommonRequest testRequest = new HttpCommonRequest();
        testRequest.setMethod(HttpMethod.GET);
        testRequest.setUrl("http://k8s.micudaj.de/pipeline/apis/v1beta1/pipelines");
        
        BearerAuthentication bearerAuth = new BearerAuthentication();
        bearerAuth.setToken(accesstoken);
        testRequest.setAuthentication(bearerAuth);


        try {
            result = httpService.executeConnectorRequest(testRequest);

            String a = "";
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
