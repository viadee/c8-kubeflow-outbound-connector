package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.RunUtil;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Run;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    private RunUtil runUtil;

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
        this.runUtil = new RunUtil();
    }

    @Override
    public HttpResponse<String> execute(HttpClient httpClient) {
        HttpResponse<String> runByIdHttpResult = super.execute(httpClient);

        Object apiRunResponse = null;
        try {
            apiRunResponse = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
                runUtil.readV1RunAsTypedResponse(runByIdHttpResult) :
                runUtil.readV2RunAsTypedResponse(runByIdHttpResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (apiRunResponse == null) {
            throw new RuntimeException("Could not identify run by id");
        }

        return runByIdHttpResult;
    }

    @Override
    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        var kubeflowUrlPath = String.format("%s/%s",
            String.format(kubeflowApiOperationsEnum.getApiUrl(), kubeflowApisEnum.getUrlPathVersion()),
            connectorRequest.kubeflowapi().runId());
        uriBuilder.setPath(kubeflowUrlPath);
    }

    public V1ApiRun getRunByIdV1Typed(HttpClient httpClient)
        throws IOException, InstantiationException, IllegalAccessException {
        var httpResponse = this.execute(httpClient);
        return runUtil.readV1RunAsTypedResponse(httpResponse);
    }

    public V2beta1Run getRunByIdV2Typed(HttpClient httpClient)
        throws IOException, InstantiationException, IllegalAccessException {
        var httpResponse = this.execute(httpClient);
        return runUtil.readV2RunAsTypedResponse(httpResponse);
    }
}
