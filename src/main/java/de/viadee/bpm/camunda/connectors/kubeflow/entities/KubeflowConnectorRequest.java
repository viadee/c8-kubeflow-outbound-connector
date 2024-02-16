package de.viadee.bpm.camunda.connectors.kubeflow.entities;

import java.util.Objects;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class KubeflowConnectorRequest {
    @Valid
    private Authentication authentication;
    @Valid
    private Configuration configuration;
    @Valid
    @NotNull
    private KubeflowApi kubeflowapi;
    @TemplateProperty(group = "timeout", defaultValue = "20", optional = false, description = "Sets the timeout in seconds to establish a connection or 0 for an infinite timeout")
    private Integer connectionTimeoutInSeconds;

    public KubeflowConnectorRequest(){};

    public KubeflowConnectorRequest(Authentication authentication, Configuration configuration,
            KubeflowApi kubeflowapi, Integer connectionTimeoutInSeconds) {
        this.authentication = authentication;
        this.configuration = configuration;
        this.kubeflowapi = kubeflowapi;
        this.connectionTimeoutInSeconds = connectionTimeoutInSeconds;
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public KubeflowApi getKubeflowapi() {
        return this.kubeflowapi;
    }

    public void setKubeflowapi(KubeflowApi kubeflowapi) {
        this.kubeflowapi = kubeflowapi;
    }

    public Integer getConnectionTimeoutInSeconds() {
        return this.connectionTimeoutInSeconds;
    }

    public void setConnectionTimeoutInSeconds(Integer connectionTimeoutInSeconds) {
        this.connectionTimeoutInSeconds = connectionTimeoutInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
            KubeflowConnectorRequest that = (KubeflowConnectorRequest) o;
        return Objects.equals(authentication, that.authentication)
                && Objects.equals(connectionTimeoutInSeconds, that.connectionTimeoutInSeconds)
                && Objects.equals(configuration, that.configuration)
                && Objects.equals(kubeflowapi, that.kubeflowapi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                authentication, connectionTimeoutInSeconds, configuration, kubeflowapi);
    }

    @Override
    public String toString() {
        return "KubeflowConnectorRequest{"
                + ", authentication="
                + authentication
                + ", connectionTimeoutInSeconds='"
                + connectionTimeoutInSeconds
                + '\''
                + ", configuration="
                + configuration
                + ", kubeflowapi="
                + kubeflowapi
                + '}';
    }
}
