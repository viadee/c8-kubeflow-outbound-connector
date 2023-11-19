package de.viadee.bpm.camunda.connectors.kubeflow.service.async;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutor;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunById;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorGetRunByName;
import de.viadee.bpm.camunda.connectors.kubeflow.service.KubeflowConnectorExecutorStartRun;

public class ExecutionHandler {
    public static KubeflowConnectorExecutor getExecutor(KubeflowConnectorRequest connectorRequest, long processInstanceKey) {
        switch (KubeflowApiOperationsEnum.fromValue(connectorRequest.kubeflowapi().operation())) {
            case GET_RUN_BY_ID:
                return new KubeflowConnectorExecutorGetRunById(connectorRequest, processInstanceKey);
            case GET_RUN_BY_NAME:
                return new KubeflowConnectorExecutorGetRunByName(connectorRequest, processInstanceKey);
            case START_RUN:
                return new KubeflowConnectorExecutorStartRun(connectorRequest, processInstanceKey);
            default:
                return new KubeflowConnectorExecutor(connectorRequest, processInstanceKey);
        }
    }
}
