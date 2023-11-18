package de.viadee.bpm.camunda.connectors.kubeflow.service.payload;

import java.util.ArrayList;
import java.util.List;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines.PipelineSpec;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines.ResourceReference;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines.ResourceReferenceKey;

public class KubeflowStartRunPayload {
    private String name;
    private PipelineSpec pipeline_spec;
    private List<ResourceReference> resource_references = new ArrayList<>();

    public KubeflowStartRunPayload(String name) {
        this.name = name;
        this.pipeline_spec = new PipelineSpec("f9f525c4-0e0b-4f57-9112-74ef8736cf6f");
        ResourceReferenceKey resourceReferenceKey = new ResourceReferenceKey();
        resourceReferenceKey.setId("32f78be0-43fa-4091-a8e9-71c667a6048e");
        resourceReferenceKey.setType("EXPERIMENT");
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setName("test");
        resourceReference.setKey(resourceReferenceKey);
        resourceReference.setRelationship("OWNER");
        this.resource_references.add(resourceReference);
    }

    /**
     * @return String return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return PipelineSpec return the pipeline_spec
     */
    public PipelineSpec getPipeline_spec() {
        return pipeline_spec;
    }

    /**
     * @param pipeline_spec the pipeline_spec to set
     */
    public void setPipeline_spec(PipelineSpec pipeline_spec) {
        this.pipeline_spec = pipeline_spec;
    }

    /**
     * @return ResourceReference return the resource_references
     */
    public List<ResourceReference> getResource_references() {
        return resource_references;
    }

    /**
     * @param resource_references the resource_references to set
     */
    public void setResource_references(List<ResourceReference> resource_references) {
        this.resource_references = resource_references;
    }
}
