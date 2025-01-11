package com.phanta.paicentre.address;

import com.phanta.paicentre.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Addresses") // Table name in snake_case
public class Address {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in snake_case
    private User user;

    @Column(name = "street", nullable = false) // Explicit column name in snake_case
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 20) // Explicit column name in snake_case
    private String postalCode;

    @Column(name = "latitude") // Optional fields, no need for `nullable = false`
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false) // Explicit column name in snake_case
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false) // Explicit column name in snake_case
    private LocalDateTime updatedAt = LocalDateTime.now();


    @PrePersist
    public void generateId() {
        if (this.id == null) {
            // Generate the ID in the format "USR_<UUID>"
            this.id = "ADD_" + UUID.randomUUID().toString();
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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
