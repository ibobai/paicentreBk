package com.phanta.paicentre.userPreference;


import java.math.BigDecimal;

public class UserPreferencesDTO {
    private String id;
    private String userId;
    private String language;
    private String currency;
    private Boolean notificationsEnabled;
    private Boolean taxingPeople;
    private BigDecimal taxPercentage;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Boolean getTaxingPeople() {
        return taxingPeople;
    }

    public void setTaxingPeople(Boolean taxingPeople) {
        this.taxingPeople = taxingPeople;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }
}
