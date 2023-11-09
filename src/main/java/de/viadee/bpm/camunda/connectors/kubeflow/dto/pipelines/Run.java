package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Run {
    private String id;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("scheduled_at")
    private String scheduledAt;

     @SerializedName("finished_at")
    private String finishedAt;

    private String name;
    
    private String description;

    private String status;

    @SerializedName("resource_references")
    private List<ResourceReference> resourceReferences;

    /**
     * @return String return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return String return the createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return String return the scheduledAt
     */
    public String getScheduledAt() {
        return scheduledAt;
    }

    /**
     * @param scheduledAt the scheduledAt to set
     */
    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    /**
     * @return String return the finishedAt
     */
    public String getFinishedAt() {
        return finishedAt;
    }

    /**
     * @param finishedAt the finishedAt to set
     */
    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
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
     * @return String return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return String return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return List<ResourceReference> return the resourceReferences
     */
    public List<ResourceReference> getResourceReferences() {
        return resourceReferences;
    }

    /**
     * @param resourceReferences the resourceReferences to set
     */
    public void setResourceReferences(List<ResourceReference> resourceReferences) {
        this.resourceReferences = resourceReferences;
    }

}
