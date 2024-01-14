package de.viadee.bpm.camunda.connectors.kubeflow;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.http.HttpRequestFactory;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.service.async.ExecutionHandler;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.annotation.ElementTemplate;

@OutboundConnector(name = "Kubeflow Connector", inputVariables = { "authentication", "configuration",
    "kubeflowapi" }, type = "de.viadee.bpm.camunda:connector-kubeflow:1")
@ElementTemplate(id = "de.viadee.bpm.camunda.connectors.kubeflow.v1", name = "Kubeflow connector", version = 1, description = "Connector for communicating with Kubeflow from Camunda", icon = "icon.svg", documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/", propertyGroups = {
    @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
    @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration"),
    @ElementTemplate.PropertyGroup(id = "kubeflowapi", label = "Kubeflow API")
}, inputDataClass = KubeflowConnectorRequest.class)
public class KubeflowConnectorFunction implements OutboundConnectorFunction {

  private final HttpClient httpClient;
  private static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModules(new JacksonModuleFeelFunction(), new Jdk8Module(), new JavaTimeModule())
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
      .build();

  public KubeflowConnectorFunction() {
    this(
        objectMapper.copy(),
        HttpTransportComponentSupplier.httpRequestFactoryInstance());
  }

  public KubeflowConnectorFunction(
      final ObjectMapper objectMapper, final HttpRequestFactory requestFactory) {
    this.httpService = new HttpService(objectMapper, requestFactory);
  }

  @Override
  public Object execute(final OutboundConnectorContext context)
      throws IOException, InstantiationException, IllegalAccessException {

    // The client ID provisioned by the OpenID provider when
    // the client was registered
    ClientID clientID = new ClientID("123");

    // The client callback URL
    URI callback = new URI("https://client.com/callback");

    // Generate random state string to securely pair the callback to this request
    State state = new State();

    // Generate nonce for the ID token
    Nonce nonce = new Nonce();

    // Compose the OpenID authentication request (for the code flow)
    AuthenticationRequest request = new AuthenticationRequest.Builder(
        new ResponseType("code"),
        new Scope("openid"),
        clientID,
        callback)
        .endpointURI(new URI("https://c2id.com/login"))
        .state(state)
        .nonce(nonce)
        .build();

    // The URI to send the user-user browser to the OpenID provider
    // E.g.
    // https://c2id.com/login?
    // client_id=123
    // &response_type=code
    // &scope=openid
    // &redirect_uri=https%3A%2F%2Fclient.com%2Fcallback
    // &state=6SK5S15Lwdp3Pem_55m-ayudGwno0eglKq6ZEWaykG8
    // &nonce=d_Y4LmbzpNHTkzTKJv6v59-OmqB_F2kNr8CbL-R2xWI
    System.out.println(request.toURI());

    final var connectorRequest = context.bindVariables(KubeflowConnectorRequest.class);
    long processInstanceKey = context.getJobContext().getProcessInstanceKey();

    KubeflowConnectorExecutor connectorExecutor = ExecutionHandler.getExecutor(connectorRequest, processInstanceKey);
    HttpCommonResult result = connectorExecutor.execute(httpService);
    return result;
  }
}
