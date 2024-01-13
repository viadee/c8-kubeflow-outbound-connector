package de.viadee.bpm.camunda.connectors.kubeflow.services;

import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.RunUtil;
import io.camunda.connector.http.base.model.HttpCommonResult;
import io.camunda.connector.http.base.services.HttpService;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1Run;
import java.io.IOException;
import org.apache.http.client.utils.URIBuilder;

public class KubeflowConnectorExecutorGetRunById extends KubeflowConnectorExecutor {

    private RunUtil runUtil;

    public KubeflowConnectorExecutorGetRunById(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
        this.runUtil = new RunUtil();
    }

    @Override
    public HttpCommonResult execute(HttpService httpService)
        throws InstantiationException, IllegalAccessException, IOException {
        HttpCommonResult runByIdHttpResult = super.execute(httpService);

        var apiRunResponse = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum) ?
            runUtil.readV1RunAsTypedResponse(runByIdHttpResult) :
            runUtil.readV2RunAsTypedResponse(runByIdHttpResult);

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

    public V1ApiRun getRunByIdV1Typed(HttpService httpService)
        throws IOException, InstantiationException, IllegalAccessException {
        var httpCommonResult = this.execute(httpService);
        return runUtil.readV1RunAsTypedResponse(httpCommonResult);
    }

    public V2beta1Run getRunByIdV2Typed(HttpService httpService)
        throws IOException, InstantiationException, IllegalAccessException {
        var httpCommonResult = this.execute(httpService);
        return runUtil.readV2RunAsTypedResponse(httpCommonResult);
    }
}
