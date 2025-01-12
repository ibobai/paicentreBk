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



//    public User updateUser(String id, UserRequestDTO userRequestDTO) {
//        Optional<User> existingUserOpt = userRepository.findById(id);
//
//        if (existingUserOpt.isEmpty()) {
//            throw new RuntimeException("User not found with ID: " + id);
//        }
//
//        User existingUser = existingUserOpt.get();
//
//        // Update personal details
//        if (userRequestDTO.getPersonal() != null) {
//            if (userRequestDTO.getPersonal().getFirstName() != null) {
//                existingUser.getPersonal().setFirstName(userRequestDTO.getPersonal().getFirstName());
//            }
//            if (userRequestDTO.getPersonal().getLastName() != null) {
//                existingUser.getPersonal().setLastName(userRequestDTO.getPersonal().getLastName());
//            }
//            if (userRequestDTO.getPersonal().getEmail() != null) {
//                existingUser.getPersonal().setEmail(userRequestDTO.getPersonal().getEmail());
//            }
//            if (userRequestDTO.getPersonal().getPhone() != null) {
//                existingUser.getPersonal().setPhone(userRequestDTO.getPersonal().getPhone());
//            }
//            if (userRequestDTO.getPersonal().getPassword() != null) {
//                existingUser.getPersonal().setPassword(userRequestDTO.getPersonal().getPassword());
//            }
//            if (userRequestDTO.getPersonal().getSex() != null) {
//                existingUser.getPersonal().setSex(userRequestDTO.getPersonal().getSex());
//            }
//        }
//
//        // Update business details
//        if (userRequestDTO.getBusiness() != null) {
//            if (userRequestDTO.getBusiness().getType() != null) {
//                existingUser.getBusiness().setType(userRequestDTO.getBusiness().getType());
//            }
//            if (userRequestDTO.getBusiness().getCompanyType() != null) {
//                existingUser.getBusiness().setCompanyType(userRequestDTO.getBusiness().getCompanyType());
//            }
//            if (userRequestDTO.getBusiness().getActivityType() != null) {
//                existingUser.getBusiness().setActivityType(userRequestDTO.getBusiness().getActivityType());
//            }
//        }
//
//        // Update address details
//        if (userRequestDTO.getAddress() != null) {
//            if (userRequestDTO.getAddress().getStreet() != null) {
//                existingUser.getAddress().setStreet(userRequestDTO.getAddress().getStreet());
//            }
//            if (userRequestDTO.getAddress().getCity() != null) {
//                existingUser.getAddress().setCity(userRequestDTO.getAddress().getCity());
//            }
//            if (userRequestDTO.getAddress().getState() != null) {
//                existingUser.getAddress().setState(userRequestDTO.getAddress().getState());
//            }
//            if (userRequestDTO.getAddress().getCountry() != null) {
//                existingUser.getAddress().setCountry(userRequestDTO.getAddress().getCountry());
//            }
//            if (userRequestDTO.getAddress().getPostalCode() != null) {
//                existingUser.getAddress().setPostalCode(userRequestDTO.getAddress().getPostalCode());
//            }
//        }
//
//        // Update preferences
//        if (userRequestDTO.getPreferences() != null) {
//            if (userRequestDTO.getPreferences().isAcceptTerms()) {
//                existingUser.getPreferences().setAcceptTerms(userRequestDTO.getPreferences().isAcceptTerms());
//            }
//            if (userRequestDTO.getPreferences().isNewsletter()) {
//                existingUser.getPreferences().setNewsletter(userRequestDTO.getPreferences().isNewsletter());
//            }
//        }
//
//        // Save and return the updated user
//        return userRepository.save(existingUser);
//    }





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
     * @param email    The email of the user
     * @param password The password of the user
     * @return ResponseEntity containing the validation result and tokens
     */
    public ResponseEntity<Map<String, Object>> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        Map<String, Object> response = new HashMap<>();

        // If the user is found
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if the password matches (you should ideally hash passwords)
            if (password.equals(user.getPassword())) {

                // Generate the access token and refresh token
                String accessToken = jwtTokenUtil.generateAccessToken(email, user.getRole());
                String refreshToken = jwtTokenUtil.generateRefreshToken(email);

                // Prepare the response with the tokens
                response.put("valid", true);
                response.put("accessToken", accessToken);
                response.put("refreshToken", refreshToken);

                return ResponseEntity.ok(response);  // Return 200 OK with tokens
            } else {
                response.put("valid", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);  // Return 401 Unauthorized
            }
        }

        // If user is not found
        response.put("valid", false);
        response.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);  // Return 401 Unauthorized
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
            userPreferences.setNotificationsEnabled(userRequest.getUserPreferences().isNotificationsEnabled());
            userPreferences.setTaxingPeople(userRequest.getUserPreferences().isTaxingPeople());
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