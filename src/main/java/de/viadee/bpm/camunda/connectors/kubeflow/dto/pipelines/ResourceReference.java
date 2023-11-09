package de.viadee.bpm.camunda.connectors.kubeflow.dto.pipelines;

public class ResourceReference {
    
    private ResourceReferenceKey key;
    private String name;
    private String relationship;

    /**
     * @return ResourceReferenceKey return the key
     */
    public ResourceReferenceKey getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(ResourceReferenceKey key) {
        this.key = key;
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
     * @return String return the relationship
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * @param relationship the relationship to set
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

}
