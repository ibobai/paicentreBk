package com.phanta.paicentre.user;

public class UserRequestDTO {
    private PersonalInfo personal;
    private BusinessInfo business;
    private AddressInfo address;
    private PreferencesInfo preferences;



    public PersonalInfo getPersonal() {
        return personal;
    }

    public void setPersonal(PersonalInfo personal) {
        this.personal = personal;
    }

    public BusinessInfo getBusiness() {
        return business;
    }

    public void setBusiness(BusinessInfo business) {
        this.business = business;
    }

    public AddressInfo getAddress() {
        return address;
    }

    public void setAddress(AddressInfo address) {
        this.address = address;
    }

    public PreferencesInfo getPreferences() {
        return preferences;
    }

    public void setPreferences(PreferencesInfo preferences) {
        this.preferences = preferences;
    }

    // Getters and setters for all fields

    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String password;
        private String sex;

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
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

        // Getters and setters
    }

    public static class BusinessInfo {
        private String type;
        private String companyType;
        private String activityType;

        public String getActivityType() {
            return activityType;
        }

        public void setActivityType(String activityType) {
            this.activityType = activityType;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        // Getters and setters
    }

    public static class AddressInfo {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

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

        // Getters and setters
    }

    public static class PreferencesInfo {
        private boolean acceptTerms;
        private boolean newsletter;

        public boolean isAcceptTerms() {
            return acceptTerms;
        }

        public void setAcceptTerms(boolean acceptTerms) {
            this.acceptTerms = acceptTerms;
        }

        public boolean isNewsletter() {
            return newsletter;
        }

        public void setNewsletter(boolean newsletter) {
            this.newsletter = newsletter;
        }

        // Getters and setters
    }
}
