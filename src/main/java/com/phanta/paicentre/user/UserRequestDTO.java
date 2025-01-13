package com.phanta.paicentre.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phanta.paicentre.address.AddressDTO;
import com.phanta.paicentre.address.AddressRepository;
import com.phanta.paicentre.oauthToken.JwtTokenUtil;
import com.phanta.paicentre.profile.ProfileDTO;
import com.phanta.paicentre.profile.ProfileRepository;
import com.phanta.paicentre.userPreference.UserPreferencesDTO;


import com.phanta.paicentre.address.Address;
import com.phanta.paicentre.profile.Profile;
import com.phanta.paicentre.userPreference.UserPreferences;
import com.phanta.paicentre.userPreference.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String sex;
    private Boolean isActive;
    private String role;
    private LocalDate dateOfBirth;
    private AddressInfo address;
    private UserPreferencesInfo userPreferences;
    private ProfileInfo profile;

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    @Autowired
    public UserRequestDTO(UserRepository userRepository,
                          UserPreferencesRepository userPreferencesRepository,
                          ProfileRepository profileRepository,
                          AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.profileRepository = profileRepository;
        this.addressRepository = addressRepository;
    }


    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public AddressInfo getAddress() {
        return address;
    }

    public void setAddress(AddressInfo address) {
        this.address = address;
    }

    public UserPreferencesInfo getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferencesInfo userPreferences) {
        this.userPreferences = userPreferences;
    }

    public ProfileInfo getProfile() {
        return profile;
    }

    public void setProfile(ProfileInfo profile) {
        this.profile = profile;
    }

    public static class AddressInfo {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private double latitude;
        private double longitude;

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

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    public static class UserPreferencesInfo {
        private String language;
        private String currency;
        private Boolean notificationsEnabled;
        private Boolean isTaxingPeople;
        private BigDecimal taxPercentage;

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

        public Boolean getIsNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(Boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }

        public Boolean getIsTaxingPeople() {
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
    }

    public static class ProfileInfo {
        private String profilePictureUrl;
        private Boolean isSelfEmployed;
        private String companyType;
        private String activityType;

        public String getProfilePictureUrl() {
            return profilePictureUrl;
        }

        public void setProfilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
        }

        public Boolean isSelfEmployed() {
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
    }


    public static UserResponseDTO getSavedUserDTO(User savedUser){
        // Return the saved user with all relationships
        // After saving the user and all related objects
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(savedUser.getId());
        responseDTO.setFirstName(savedUser.getFirstName());
        responseDTO.setLastName(savedUser.getLastName());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setPhoneNumber(savedUser.getPhoneNumber());
        responseDTO.setSex(savedUser.getSex());
        responseDTO.setDateOfBirth(savedUser.getDateOfBirth().toString());
        responseDTO.setCreatedAt(savedUser.getCreatedAt());
        responseDTO.setUpdatedAt(savedUser.getUpdatedAt());
        responseDTO.setIsActive(savedUser.getIsActive());
        responseDTO.setRole(savedUser.getRole());


        // Add UserPreferences if available
        if (savedUser.getPreferences() != null) {
            UserPreferencesDTO userPreferencesDTO = new UserPreferencesDTO();
            userPreferencesDTO.setLanguage(savedUser.getPreferences().getLanguage());
            userPreferencesDTO.setId(savedUser.getPreferences().getId());
            userPreferencesDTO.setUserId(savedUser.getId());
            userPreferencesDTO.setCurrency(savedUser.getPreferences().getCurrency());
            userPreferencesDTO.setNotificationsEnabled(savedUser.getPreferences().getNotificationsEnabled());
            userPreferencesDTO.setTaxingPeople(savedUser.getPreferences().getTaxingPeople());
            userPreferencesDTO.setTaxPercentage(savedUser.getPreferences().getTaxPercentage());

            responseDTO.setPreferences(userPreferencesDTO);
        }

        // Assuming savedUser is already fetched from the database

        // Check if the address is available
        if (savedUser.getAddress() != null) {
            // Create a new AddressDTO object
            AddressDTO addressDTO = new AddressDTO();
            // Set the address details from the saved user
            addressDTO.setUserId(savedUser.getId());  // Set userId from savedUser (this will be the user linked to the address)
            addressDTO.setStreet(savedUser.getAddress().getStreet());  // Set street from the saved user's address
            addressDTO.setCity(savedUser.getAddress().getCity());  // Set city
            addressDTO.setState(savedUser.getAddress().getState());  // Set state
            addressDTO.setCountry(savedUser.getAddress().getCountry());  // Set country
            addressDTO.setPostalCode(savedUser.getAddress().getPostalCode());  // Set postalCode
            addressDTO.setLatitude(savedUser.getAddress().getLatitude());  // Set latitude
            addressDTO.setLongitude(savedUser.getAddress().getLongitude());  // Set longitude
            addressDTO.setId(savedUser.getAddress().getId());
            // Set the addressDTO in the responseDTO
            responseDTO.setAddress(addressDTO);
        }


        // Assuming savedUser is already fetched from the database

        // Check if the profile is available
        if (savedUser.getProfile() != null) {
            // Create a new ProfileDTO object
            ProfileDTO profileDTO = new ProfileDTO();

            // Set profile details from the saved user
            profileDTO.setUserId(savedUser.getId());  // Set the userId from savedUser (this links the profile to the user)
            profileDTO.setProfilePictureUrl(savedUser.getProfile().getProfilePictureUrl());  // Set profile picture URL
            profileDTO.setIsSelfEmployed(savedUser.getProfile().getSelfEmployed());  // Set self-employment status
            profileDTO.setCompanyType(savedUser.getProfile().getCompanyType());  // Set company type
            profileDTO.setActivityType(savedUser.getProfile().getActivityType());  // Set activity type
            profileDTO.setId(savedUser.getProfile().getId());  // Set activity type
            // Set the profileDTO in the responseDTO
            responseDTO.setProfile(profileDTO);
        }


        // Return the custom response DTO
        return responseDTO;
    }




}
