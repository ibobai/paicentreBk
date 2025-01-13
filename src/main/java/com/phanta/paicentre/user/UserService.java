package com.phanta.paicentre.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phanta.paicentre.address.Address;
import com.phanta.paicentre.address.AddressDTO;
import com.phanta.paicentre.address.AddressRepository;
import com.phanta.paicentre.oauthToken.JwtTokenUtil;
import com.phanta.paicentre.profile.Profile;
import com.phanta.paicentre.profile.ProfileDTO;
import com.phanta.paicentre.profile.ProfileRepository;
import com.phanta.paicentre.userPreference.UserPreferences;
import com.phanta.paicentre.userPreference.UserPreferencesDTO;
import com.phanta.paicentre.userPreference.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.phanta.paicentre.user.UserRequestDTO;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Used for JSON parsing
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AddressRepository addressRepository;


    @Autowired
    public UserService(UserRepository userRepository, ObjectMapper objectMapper, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public ResponseEntity<?> createUser(User user) {
        // Check if the email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            // Return a bad request response with the existing user's ID
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", "User already exists",
                            "userId", "USR_" + existingUser.get().getId()
                    )
            );
        }

        // Save the new user
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @Transactional
    public ResponseEntity<?> createUserDTO(UserRequestDTO userRequest) {
        try{
            // Check if the email already exists
            Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
            if (existingUser.isPresent()) {
                // Return a bad request response with the existing user's ID
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.BAD_REQUEST.value(),
                                "error", "User already exists",
                                "message", "User already exists with ID: " + existingUser.get().getId(),
                                "path", "/api/user/createDTO"
                        )
                );
            }


            // Map the personal info from the DTO to the User object
            return ResponseEntity.ok(UserRequestDTO.getSavedUserDTO(mapAndSaveUserWithAllDetails(userRequest)));


        } catch (Exception ex) {
            // Return a bad request response with the error message
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "timestamp", LocalDateTime.now(),
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "error", "Unable to create user",
                            "message", ex.getMessage(),
                            "path", "/api/user/createDTO"
                    )
            );
        }

    }


    /**
     * Updates an existing user based on the provided ID and UserRequestDTO.
     *
     * @param id             The ID of the user to update.
     * @param userRequestDTO The request DTO containing the fields to update.
     * @return The updated user in the form of a UserResponseDTO.
     * @throws RuntimeException If the user with the given ID is not found.
     */
    public UserResponseDTO updateUser(String id, UserRequestDTO userRequestDTO) {
        Optional<User> existingUserOpt = userRepository.findById(id);

        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
            //UserRequestValidator.buildErrorResponse("User not found with ID: " + id, "/api/user/update/"+id);
            //return null;
        }

        User existingUser = existingUserOpt.get();

        // Update fields only if they are not null or empty in the DTO
        if (userRequestDTO.getFirstName() != null && !userRequestDTO.getFirstName().isBlank()) {
            existingUser.setFirstName(userRequestDTO.getFirstName());
        }
        if (userRequestDTO.getLastName() != null && !userRequestDTO.getLastName().isBlank()) {
            existingUser.setLastName(userRequestDTO.getLastName());
        }
        if (userRequestDTO.getEmail() != null && !userRequestDTO.getEmail().isBlank()) {
            existingUser.setEmail(userRequestDTO.getEmail());
        }
        if (userRequestDTO.getPhoneNumber() != null && !userRequestDTO.getPhoneNumber().isBlank()) {
            existingUser.setPhoneNumber(userRequestDTO.getPhoneNumber());
        }
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isBlank()) {
            existingUser.setPassword(userRequestDTO.getPassword());
        }
        if (userRequestDTO.getSex() != null && !userRequestDTO.getSex().isBlank()) {
            existingUser.setSex(userRequestDTO.getSex());
        }
        if (userRequestDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(userRequestDTO.getDateOfBirth());
        }
        if (userRequestDTO.getIsActive() != null) {
            existingUser.setIsActive(userRequestDTO.getIsActive());
        }
        if (userRequestDTO.getRole() != null) {
            existingUser.setRole(userRequestDTO.getRole());
        }


        // Update address if provided
        if (userRequestDTO.getAddress() != null) {
            UserRequestDTO.AddressInfo addressInfo = userRequestDTO.getAddress();
            Address address = existingUser.getAddress();
            if (address == null) {
                address = new Address();
                existingUser.setAddress(address);
            }
            if (addressInfo.getStreet() != null && !addressInfo.getStreet().isBlank()) {
                address.setStreet(addressInfo.getStreet());
            }
            if (addressInfo.getCity() != null && !addressInfo.getCity().isBlank()) {
                address.setCity(addressInfo.getCity());
            }
            if (addressInfo.getState() != null && !addressInfo.getState().isBlank()) {
                address.setState(addressInfo.getState());
            }
            if (addressInfo.getCountry() != null && !addressInfo.getCountry().isBlank()) {
                address.setCountry(addressInfo.getCountry());
            }
            if (addressInfo.getPostalCode() != null && !addressInfo.getPostalCode().isBlank()) {
                address.setPostalCode(addressInfo.getPostalCode());
            }
            if (addressInfo.getLatitude() != 0.0) { // or some other default value
                address.setLatitude(addressInfo.getLatitude());
            }
            if (addressInfo.getLongitude() != 0.0) { // or some other default value
                address.setLongitude(addressInfo.getLongitude());
            }

        }

        // Update preferences if provided
        if (userRequestDTO.getUserPreferences() != null) {
            UserRequestDTO.UserPreferencesInfo preferencesInfo = userRequestDTO.getUserPreferences();
            UserPreferences preferences = existingUser.getPreferences();
            if (preferences == null) {
                preferences = new UserPreferences();
                existingUser.setPreferences(preferences);
            }
            if (preferencesInfo.getLanguage() != null && !preferencesInfo.getLanguage().isBlank()) {
                preferences.setLanguage(preferencesInfo.getLanguage());
            }
            if (preferencesInfo.getCurrency() != null && !preferencesInfo.getCurrency().isBlank()) {
                preferences.setCurrency(preferencesInfo.getCurrency());
            }
            if (preferencesInfo.getIsNotificationsEnabled() != null) {
                preferences.setNotificationsEnabled(preferencesInfo.getIsNotificationsEnabled());
            }
            if (preferencesInfo.getIsTaxingPeople() != null) {
                preferences.setTaxingPeople(preferencesInfo.getIsTaxingPeople());
            }
            if (preferencesInfo.getTaxPercentage() != null) {
                preferences.setTaxPercentage(preferencesInfo.getTaxPercentage());
            }
        }
        // Update Profile
        if (userRequestDTO.getProfile() != null) {
            UserRequestDTO.ProfileInfo profileInfo = userRequestDTO.getProfile();
            Profile profile = existingUser.getProfile();
            if (profile == null) {
                profile = new Profile();
                existingUser.setProfile(profile);
            }

            if (profileInfo.getProfilePictureUrl() != null) {
                profile.setProfilePictureUrl(profileInfo.getProfilePictureUrl());
            }
            if (profileInfo.isSelfEmployed() != null) {
                profile.setSelfEmployed(profileInfo.isSelfEmployed());
            }
            if (profileInfo.getCompanyType() != null) {
                profile.setCompanyType(profileInfo.getCompanyType());
            }
            if (profileInfo.getActivityType() != null) {
                profile.setActivityType(profileInfo.getActivityType());
            }
        }

        // Save and return the updated user
        User savedUser = userRepository.save(existingUser);
        return UserRequestDTO.getSavedUserDTO(savedUser);
    }

    /**
     * Checks if a UserRequestDTO is empty (all fields are null or blank).
     *
     * @param userRequestDTO The request DTO to check.
     * @return True if the DTO is empty, false otherwise.
     */
    public boolean isEmpty(UserRequestDTO userRequestDTO) {
        return (userRequestDTO.getFirstName() == null || userRequestDTO.getFirstName().isBlank()) &&
                (userRequestDTO.getLastName() == null || userRequestDTO.getLastName().isBlank()) &&
                (userRequestDTO.getEmail() == null || userRequestDTO.getEmail().isBlank()) &&
                (userRequestDTO.getPhoneNumber() == null || userRequestDTO.getPhoneNumber().isBlank()) &&
                (userRequestDTO.getPassword() == null || userRequestDTO.getPassword().isBlank()) &&
                userRequestDTO.getSex() == null &&
                userRequestDTO.getDateOfBirth() == null &&
                userRequestDTO.getAddress() == null &&
                userRequestDTO.getUserPreferences() == null &&
                userRequestDTO.getProfile() == null;
    }





    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }


    /**
     * Validates an email address extracted from the request body.
     *
     * @param requestBody The object containing email details.
     * @return A Map with the validation result.
     */
    public Map<String, Boolean> validateEmail(Object requestBody) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            // Convert the request body to a map
            Map<String, Object> requestMap = objectMapper.convertValue(requestBody, Map.class);

            // Extract the email field
            String email = (String) requestMap.get("email");

            // Perform basic email validation
            if (email == null || !email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$")) {
                response.put("valid", false); // Invalid email format
                return response;
            }

            // Check if the email already exists in the database
            boolean isValid = userRepository.findByEmail(email).isEmpty();
            response.put("valid", isValid);
            return response;
        } catch (Exception e) {
            // Log the exception and return a validation failure
            e.printStackTrace();
            response.put("valid", false);
            return response;
        }
    }

    /**
     * Authenticates the user and returns the access token and refresh token.
     *
     * @param email    The email of the user extracted from Basic Auth
     * @param password The password of the user extracted from Basic Auth
     * @return ResponseEntity containing the validation result and tokens
     */
    public ResponseEntity<Map<String, Object>> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        Map<String, Object> response = new HashMap<>();

        // If the user is found
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the password matches (hashing is recommended in production)
            if (password.equals(user.getPassword())) {

                // Generate the access token and refresh token
                String accessToken = jwtTokenUtil.generateAccessToken(email, user.getRole());
                String refreshToken = jwtTokenUtil.generateRefreshToken(email);

                // Prepare the response with the tokens
                response.put("valid", true);
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);

                return ResponseEntity.ok(response); // Return 200 OK with tokens
            } else {
                response.put("valid", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Return 401 Unauthorized
            }
        } else {
            response.put("valid", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Return 404 Not Found
        }
    }


    public User mapAndSaveUserWithAllDetails(UserRequestDTO userRequest) {
        // Map and save basic user details and generate ID
        User savedUser = mapAndSaveUserDetails(userRequest);

        // Map and save preferences if provided
        mapAndSaveUserPreferences(userRequest, savedUser);

        // Map and save address if provided
        mapAndSaveUserAddress(userRequest, savedUser);

        // Map and save profile if provided
        mapAndSaveUserProfile(userRequest, savedUser);

        return savedUser;
    }

    private User mapAndSaveUserDetails(UserRequestDTO userRequest) {
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setPassword(userRequest.getPassword());
        user.setSex(userRequest.getSex().toUpperCase());
        user.setIsActive(userRequest.getIsActive());
        user.setRole(userRequest.getRole());
        user.setDateOfBirth(userRequest.getDateOfBirth());
        return userRepository.save(user); // Save and return the user to generate the ID
    }

    private void mapAndSaveUserPreferences(UserRequestDTO userRequest, User savedUser) {
        if (userRequest.getUserPreferences() != null) {
            UserPreferences userPreferences = new UserPreferences();
            userPreferences.setLanguage(userRequest.getUserPreferences().getLanguage());
            userPreferences.setCurrency(userRequest.getUserPreferences().getCurrency());
            userPreferences.setNotificationsEnabled(userRequest.getUserPreferences().getIsNotificationsEnabled());
            userPreferences.setTaxingPeople(userRequest.getUserPreferences().getIsTaxingPeople());
            userPreferences.setTaxPercentage(userRequest.getUserPreferences().getTaxPercentage());
            userPreferences.setUser(savedUser);
            userPreferencesRepository.save(userPreferences);

            // Set preferences back to the user for reference
            savedUser.setPreferences(userPreferences);
        }
    }

    private void mapAndSaveUserAddress(UserRequestDTO userRequest, User savedUser) {
        if (userRequest.getAddress() != null) {
            Address address = new Address();
            address.setStreet(userRequest.getAddress().getStreet());
            address.setCity(userRequest.getAddress().getCity());
            address.setState(userRequest.getAddress().getState());
            address.setCountry(userRequest.getAddress().getCountry());
            address.setPostalCode(userRequest.getAddress().getPostalCode());
            address.setLatitude(userRequest.getAddress().getLatitude());
            address.setLongitude(userRequest.getAddress().getLongitude());
            address.setUser(savedUser);
            addressRepository.save(address);

            // Set address back to the user for reference
            savedUser.setAddress(address);
        }
    }

    private void mapAndSaveUserProfile(UserRequestDTO userRequest, User savedUser) {
        if (userRequest.getProfile() != null) {
            Profile profile = new Profile();
            profile.setProfilePictureUrl(userRequest.getProfile().getProfilePictureUrl());
            profile.setSelfEmployed(userRequest.getProfile().isSelfEmployed());
            profile.setCompanyType(userRequest.getProfile().getCompanyType());
            profile.setActivityType(userRequest.getProfile().getActivityType());
            profile.setUser(savedUser); // Link the profile to the user

            // Save the profile
            profileRepository.save(profile);

            // Set the profile back to the user for reference
            savedUser.setProfile(profile);
        }
    }
}