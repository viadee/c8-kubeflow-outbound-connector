package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

import java.util.List;

// TODO
public class GetRunsResult {

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
