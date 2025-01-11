package com.phanta.paicentre.profile;

public class ProfileDTO {
    private String id;
    private String userId;
    private String profilePictureUrl;
    private Boolean isSelfEmployed;
    private String companyType;
    private String activityType;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean getIsSelfEmployed() {
        return isSelfEmployed;
    }

    public void setIsSelfEmployed(Boolean isSelfEmployed) {
        this.isSelfEmployed = isSelfEmployed;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
}
