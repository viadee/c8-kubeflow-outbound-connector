package de.viadee.bpm.camunda.connectors.kubeflow.entities.input;

import java.util.Map;

import io.camunda.connector.generator.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record KubeflowApi(
    @NotEmpty
    @TemplateProperty(group = "kubeflowapi", label = "API", description = "API")
    String api,
    @NotEmpty
    @TemplateProperty(group = "kubeflowapi", label = "API Operation", description = "API operation to execute")
    String operation,
    @TemplateProperty(group = "kubeflowapi", label = "Run ID", description = "The ID of the run to get")
    String runId,
    @TemplateProperty(group = "kubeflowapi", label = "Run Name", description = "The name of the run")
    String runName,
    @TemplateProperty(group = "kubeflowapi", label = "Run Parameters", description = "The parameters of the run")
    Map<String, Object> runParameters,
    @TemplateProperty(group = "kubeflowapi", label = "Pipeline ID", description = "The ID of the pipeline to start")
    String pipelineId,
    @TemplateProperty(group = "kubeflowapi", label = "Experiment ID", description = "The ID of the experiment in which to start the run")
    String experimentId,
    @TemplateProperty(group = "kubeflowapi", label = "Filter", description = "Filter to apply")
    String filter,
    @TemplateProperty(group = "kubeflowapi", label = "Polling Interval", description = "The interval to regularly check for a change in the state")
    String pollingInterval,
    @TemplateProperty(group = "kubeflowapi", label = "Experiment Name", description = "The name of the experiment")
    String experimentName,
    @TemplateProperty(group = "kubeflowapi", label = "Experiment Description", description = "The description of the experiment")
    String experimentDescription,
    @TemplateProperty(group = "kubeflowapi", label = "HTTP Headers", description = "Headers to be sent with the request")
    Map<String, String> httpHeaders
) { }
