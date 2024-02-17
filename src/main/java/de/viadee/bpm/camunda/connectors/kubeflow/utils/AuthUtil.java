package de.viadee.bpm.camunda.connectors.kubeflow.utils;

import org.apache.commons.lang3.StringUtils;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BasicAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.BearerAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.EnvironmentAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.NoAuthentication;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationClientCredentialsFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.auth.OAuthAuthenticationPasswordFlow;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;

public class AuthUtil {

    private static final String KUBEFLOW_AUTH_MODE = "KF_AUTH_MODE";
    private static final String KUBEFLOW_AUTH_MODE_NONE = "none";
    private static final String KUBEFLOW_AUTH_MODE_BASIC = "basic";
    private static final String KUBEFLOW_AUTH_MODE_BEARER = "bearer";
    private static final String KUBEFLOW_AUTH_MODE_OAUTH_CC = "oauth_cc";
    private static final String KUBEFLOW_AUTH_MODE_OAUTH_PASSWORD = "oauth_password";
    private static final String KUBEFLOW_AUTH_BASIC_USERNAME = "KF_AUTH_BASIC_USERNAME";
    private static final String KUBEFLOW_AUTH_BASIC_PASSWORD = "KF_AUTH_BASIC_PASSWORD";
    private static final String KUBEFLOW_AUTH_BEARER_TOKEN = "KF_AUTH_BEARER_TOKEN";
    private static final String KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT = "KF_AUTH_OAUTH_TOKEN_ENDPOINT";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_ID = "KF_AUTH_OAUTH_CLIENT_ID";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET = "KF_AUTH_OAUTH_CLIENT_SECRET";
    private static final String KUBEFLOW_AUTH_OAUTH_SCOPES = "KF_AUTH_OAUTH_SCOPES";
    private static final String KUBEFLOW_AUTH_OAUTH_AUDIENCE = "KF_AUTH_OAUTH_AUDIENCE";
    private static final String KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH = "KF_AUTH_OAUTH_CLIENT_AUTH";
    private static final String KUBEFLOW_AUTH_OAUTH_USERNAME = "KF_AUTH_OAUTH_USERNAME";
    private static final String KUBEFLOW_AUTH_OAUTH_PASSWORD = "KF_AUTH_OAUTH_PASSWORD";

    public static void setAuthenticationParameters(KubeflowConnectorRequest connectorRequest) {
        Authentication authPropertyGroup = connectorRequest.getAuthentication();

        if (authPropertyGroup instanceof EnvironmentAuthentication) {
            String authMode = System.getenv(KUBEFLOW_AUTH_MODE);
            if (authMode == null || authMode.equals(KUBEFLOW_AUTH_MODE_NONE)) {
                NoAuthentication noAuthentication = new NoAuthentication();
                connectorRequest.setAuthentication(noAuthentication);
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_BASIC)) {
                BasicAuthentication basicAuthentication = (BasicAuthentication) authPropertyGroup;
                basicAuthentication.setUsername(System.getenv(KUBEFLOW_AUTH_BASIC_USERNAME));
                basicAuthentication.setPassword(System.getenv(KUBEFLOW_AUTH_BASIC_PASSWORD));
                connectorRequest.setAuthentication(basicAuthentication);

                if (StringUtils.isBlank(basicAuthentication.getUsername())
                        || StringUtils.isBlank(basicAuthentication.getPassword())) {
                    throw new RuntimeException(
                            "Authentication parameters missing for basic authentication in environment! Required are username and password.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_BEARER)) {
                BearerAuthentication bearerAuthentication = (BearerAuthentication) authPropertyGroup;
                bearerAuthentication.setToken(System.getenv(KUBEFLOW_AUTH_BEARER_TOKEN));
                connectorRequest.setAuthentication(bearerAuthentication);
                if (StringUtils.isBlank(bearerAuthentication.getToken())) {
                    throw new RuntimeException(
                            "Authentication parameters missing for bearer authentication in environment! Required is token.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_OAUTH_CC)) {
                OAuthAuthenticationClientCredentialsFlow oAuthAuthenticationClientCredentialsFlow = new OAuthAuthenticationClientCredentialsFlow();
                oAuthAuthenticationClientCredentialsFlow
                        .setOauthTokenEndpoint(System.getenv(KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT));
                oAuthAuthenticationClientCredentialsFlow.setClientId(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_ID));
                oAuthAuthenticationClientCredentialsFlow
                        .setClientSecretCC(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET));
                oAuthAuthenticationClientCredentialsFlow.setScopes(System.getenv(KUBEFLOW_AUTH_OAUTH_SCOPES));
                oAuthAuthenticationClientCredentialsFlow.setAudience(System.getenv(KUBEFLOW_AUTH_OAUTH_AUDIENCE));
                oAuthAuthenticationClientCredentialsFlow
                        .setClientAuthentication(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH));
                connectorRequest.setAuthentication(oAuthAuthenticationClientCredentialsFlow);
                if (StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getOauthTokenEndpoint())
                        || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientId())
                        || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientSecretCC())
                        || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getScopes())
                        || StringUtils.isBlank(oAuthAuthenticationClientCredentialsFlow.getClientAuthentication())) {
                    throw new RuntimeException(
                            "Authentication parameters missing for OAuth (client-credentials flow) authentication in environment! Required are token endpoint, clientId, clientSecret, scopes and client authentication.");
                }
            } else if (authMode.equals(KUBEFLOW_AUTH_MODE_OAUTH_PASSWORD)) {
                OAuthAuthenticationPasswordFlow oAuthAuthenticationPasswordFlow = (OAuthAuthenticationPasswordFlow) authPropertyGroup;
                oAuthAuthenticationPasswordFlow
                        .setOauthTokenEndpoint(System.getenv(KUBEFLOW_AUTH_OAUTH_TOKEN_ENDPOINT));
                oAuthAuthenticationPasswordFlow.setClientId(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_ID));
                oAuthAuthenticationPasswordFlow.setClientSecretPW(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_SECRET));
                oAuthAuthenticationPasswordFlow.setScopes(System.getenv(KUBEFLOW_AUTH_OAUTH_SCOPES));
                oAuthAuthenticationPasswordFlow.setAudience(System.getenv(KUBEFLOW_AUTH_OAUTH_AUDIENCE));
                oAuthAuthenticationPasswordFlow.setClientAuthentication(System.getenv(KUBEFLOW_AUTH_OAUTH_CLIENT_AUTH));
                oAuthAuthenticationPasswordFlow.setUsername(System.getenv(KUBEFLOW_AUTH_OAUTH_USERNAME));
                oAuthAuthenticationPasswordFlow.setPassword(System.getenv(KUBEFLOW_AUTH_OAUTH_PASSWORD));
                connectorRequest.setAuthentication(oAuthAuthenticationPasswordFlow);
                if (StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getOauthTokenEndpoint())
                        || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getClientId())
                        || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getScopes())
                        || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getClientAuthentication())
                        || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getUsername())
                        || StringUtils.isBlank(oAuthAuthenticationPasswordFlow.getPassword())) {
                    throw new RuntimeException(
                            "Authentication parameters missing for OAuth (password flow) authentication in environment! Required are token endpoint, clientId, scopes, client authentication, username and password.");
                }
            }
        }
    }
}
