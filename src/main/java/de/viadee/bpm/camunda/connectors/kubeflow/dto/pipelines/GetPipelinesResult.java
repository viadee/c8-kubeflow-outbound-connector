package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

import java.util.List;

// TODO
public class GetPipelinesResult {
    private List<Pipeline> pipelines;

    /**
     * @return List<Pipeline> return the pipelines
     */
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    /**
     * @param pipelines the pipelines to set
     */
    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = pipelines;
    }

}
