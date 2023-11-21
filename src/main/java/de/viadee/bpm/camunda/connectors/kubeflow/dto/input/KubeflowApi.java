package de.viadee.bpm.camunda.connectors.kubeflow.dto.input;

import io.camunda.connector.generator.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record KubeflowApi(
    @NotEmpty
    @TemplateProperty(group = "kubeflowapi", label = "API Operation", description = "API operation to execute")
    String operation,
    @TemplateProperty(group = "kubeflowapi", label = "Run ID", description = "The ID of the run to get")
    String runid,
    @TemplateProperty(group = "kubeflowapi", label = "Run Name", description = "The name of the run to get")
    String runname,
    @TemplateProperty(group = "kubeflowapi", label = "Pipeline ID", description = "The ID of the pipeline to start")
    String pipelineid,
    @TemplateProperty(group = "kubeflowapi", label = "Experiment ID", description = "The ID of the experiment in which to start the run")
    String experimentid,
    @TemplateProperty(group = "kubeflowapi", label = "Filter", description = "Filter to apply")
    String filter) { 
    }
