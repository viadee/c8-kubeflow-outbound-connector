package de.viadee.bpm.camunda.connectors.kubeflow.auth;

public enum OAuthParamsEnum {
    PASSWORD("password"),
    USERNAME("username"),
    GRANT_TYPE("grant_type"),
    CLIENT_ID("client_id"),
    CLIENT_SECRET("client_secret"),
    AUDIENCE("audience"),
    SCOPE("scope"),
    ACCESS_TOKEN("access_token"),
    BASIC_AUTH_HEADER("basicAuthHeader"),
    CREDENTIALS_BODY("credentialsBody");

    private final String param;

    OAuthParamsEnum(String param) {
        this.param = param;
    }

    public String toString() {
        return param;
    }
}
