package de.viadee.bpm.camunda.connectors.kubeflow.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.RunUtil;
import java.net.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

public class KubeflowConnectorExecutorGetExperimentById extends KubeflowConnectorExecutor {

    public KubeflowConnectorExecutorGetExperimentById(KubeflowConnectorRequest connectorRequest, long processInstanceKey, KubeflowApisEnum kubeflowApisEnum,
        KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
        super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
    }

    @Override
    protected void addKubeflowUrlPath(URIBuilder uriBuilder) {
        var kubeflowUrlPath = String.format("%s/%s",
            String.format(kubeflowApiOperationsEnum.getApiUrl(), kubeflowApisEnum.getUrlPathVersion()),
            connectorRequest.getKubeflowapi().experimentId());
        uriBuilder.setPath(kubeflowUrlPath);
    }
}
