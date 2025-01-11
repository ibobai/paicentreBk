package com.phanta.paicentre.userPreference;

import com.phanta.paicentre.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "UserPreferences") // Table name in snake_case
public class UserPreferences {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id; // Database will handle this field with a default value.

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in snake_case
    private User user;

    @Column(name = "language", length = 10) // Explicit column name in snake_case
    private String language;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "notifications_enabled", nullable = false) // Explicit column name in snake_case
    private Boolean notificationsEnabled = true;

    @Column(name = "is_taxing_people", nullable = false)
    private Boolean isTaxingPeople = false;

    @Column(name = "tax_percentage", precision = 5, scale = 2) // Explicit column name in snake_case
    private BigDecimal taxPercentage;

    @Column(name = "created_at", nullable = false, updatable = false) // Explicit column name in snake_case
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false) // Explicit column name in snake_case
    private LocalDateTime updatedAt = LocalDateTime.now();


    @PrePersist
    public void generateId() {
        if (this.id == null) {
            // Generate the ID in the format "USR_<UUID>"
            this.id = "PREF_" + UUID.randomUUID().toString();
        }
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        return isTaxingPeople;
    }

    public void setTaxingPeople(Boolean taxingPeople) {
        isTaxingPeople = taxingPeople;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
