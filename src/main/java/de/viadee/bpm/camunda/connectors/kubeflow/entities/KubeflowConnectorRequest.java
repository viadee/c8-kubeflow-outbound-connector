package de.viadee.bpm.camunda.connectors.kubeflow.entities;

import java.util.Objects;

import de.viadee.bpm.camunda.connectors.kubeflow.auth.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Configuration;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.Timeout;
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
    @Valid
    @NotNull
    private Timeout timeout;

    public KubeflowConnectorRequest(){};

    public KubeflowConnectorRequest(Authentication authentication, Configuration configuration,
            KubeflowApi kubeflowapi, Timeout timeout) {
        this.authentication = authentication;
        this.configuration = configuration;
        this.kubeflowapi = kubeflowapi;
        this.timeout = timeout;
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

    public Timeout getTimeout() {
        return this.timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
            KubeflowConnectorRequest that = (KubeflowConnectorRequest) o;
        return Objects.equals(authentication, that.authentication)
                && Objects.equals(timeout, that.timeout)
                && Objects.equals(configuration, that.configuration)
                && Objects.equals(kubeflowapi, that.kubeflowapi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                authentication, timeout, configuration, kubeflowapi);
    }

    @Override
    public String toString() {
        return "KubeflowConnectorRequest{"
                + ", authentication="
                + authentication
                + '\''
                + ", timeout='"
                + timeout
                + '\''
                + ", configuration="
                + configuration
                + '\''
                + ", kubeflowapi="
                + kubeflowapi
                + '}';
    }
}
