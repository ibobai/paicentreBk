package com.phanta.paicentre.profile;

import com.phanta.paicentre.address.Address;
import com.phanta.paicentre.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Profile") // Table name in snake_case
public class Profile {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id; // Database will handle this field with a default value.

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in snake_case
    private User user;

    @Column(name = "profile_picture_url") // Explicit column name in snake_case
    private String profilePictureUrl;

    @ManyToOne
    @JoinColumn(name = "address_id") // Foreign key column in snake_case
    private Address address;

    @Column(name = "is_self_employed", nullable = false) // Explicit column name in snake_case
    private Boolean isSelfEmployed = false;

    @Column(name = "company_type", length = 50) // Explicit column name in snake_case
    private String companyType;

    @Column(name = "activity_type") // Explicit column name in snake_case
    private String activityType;

    @Column(name = "updated_at", nullable = false) // Explicit column name in snake_case
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            // Generate the ID in the format "USR_<UUID>"
            this.id = "PROF_" + UUID.randomUUID().toString();
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

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Boolean getSelfEmployed() {
        return isSelfEmployed;
    }

    public void setSelfEmployed(Boolean selfEmployed) {
        isSelfEmployed = selfEmployed;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
