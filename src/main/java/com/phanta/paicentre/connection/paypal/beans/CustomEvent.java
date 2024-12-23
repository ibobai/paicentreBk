package com.phanta.paicentre.connection.paypal.beans;


import com.google.gson.annotations.SerializedName;

public class CustomEvent {

    @SerializedName("id")
    private String id;

    @SerializedName("event_type")
    private String eventType;

    @SerializedName("resource_type")
    private String resourceType;

    @SerializedName("summary")
    private String summary;

    @SerializedName("resource")
    private CustomResource resource;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public CustomResource getResource() {
        return resource;
    }

    public void setResource(CustomResource resource) {
        this.resource = resource;
    }
}
