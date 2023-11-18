package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

public class PipelineSpec {

    private String pipeline_id;

    public PipelineSpec(String pipeline_id) {
        this.pipeline_id = pipeline_id;
    }

    /**
     * @return String return the pipeline_id
     */
    public String getPipeline_id() {
        return pipeline_id;
    }

    /**
     * @param pipeline_id the pipeline_id to set
     */
    public void setPipeline_id(String pipeline_id) {
        this.pipeline_id = pipeline_id;
    }

}
