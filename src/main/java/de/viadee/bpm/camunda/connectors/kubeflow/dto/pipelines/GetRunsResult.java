package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

import java.util.List;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.KubeflowConnectorResponse;

public class GetRunsResult implements KubeflowConnectorResponse {

    private List<Run> runs;

    /**
     * @return List<Run> return the runs
     */
    public List<Run> getRuns() {
        return runs;
    }

    /**
     * @param runs the runs to set
     */
    public void setRuns(List<Run> runs) {
        this.runs = runs;
    }

}
