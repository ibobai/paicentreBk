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
        try {
            // Validate required fields
            if (userRequest.getFirstName() == null || userRequest.getFirstName().isEmpty()) {
                return buildErrorResponse("Field 'firstName' is required");
            }
            if (userRequest.getLastName() == null || userRequest.getLastName().isEmpty()) {
                return buildErrorResponse("Field 'lastName' is required");
            }
            if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
                return buildErrorResponse("Field 'email' is required");
            }
            if (userRequest.getPhoneNumber() == null || userRequest.getPhoneNumber().isEmpty()) {
                return buildErrorResponse("Field 'phoneNumber' is required");
            }
            if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
                return buildErrorResponse("Field 'password' is required");
            }
            if (userRequest.getSex() == null || userRequest.getSex().isEmpty()) {
                return buildErrorResponse("Field 'sex' is required");
            }
            if (userRequest.getDateOfBirth() == null || userRequest.getDateOfBirth().toString().isEmpty()) {
                return buildErrorResponse("Field 'dateOfBirth' is required");
            }

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

            // Import necessary classes


            // Map the personal info from the DTO to the User object
            User user = new User();
            user.setFirstName(userRequest.getFirstName());
            user.setLastName(userRequest.getLastName());
            user.setEmail(userRequest.getEmail());
            user.setPhoneNumber(userRequest.getPhoneNumber());
            user.setPassword(userRequest.getPassword());
            user.setSex(userRequest.getSex());
            //user.setActive(userRequest.getActive());
            //user.setRole(userRequest.getRole());
            user.setDateOfBirth(userRequest.getDateOfBirth()); // Convert the date string to LocalDate

            // Save user first to generate the ID
            User savedUser = userRepository.save(user);

            // Map preferences if available and save them
            if (userRequest.getUserPreferences() != null) {
                UserPreferences userPreferences = new UserPreferences();
                userPreferences.setLanguage(userRequest.getUserPreferences().getLanguage());
                userPreferences.setCurrency(userRequest.getUserPreferences().getCurrency());
                userPreferences.setNotificationsEnabled(userRequest.getUserPreferences().isNotificationsEnabled());
                userPreferences.setTaxingPeople(userRequest.getUserPreferences().isTaxingPeople());
                userPreferences.setTaxPercentage(userRequest.getUserPreferences().getTaxPercentage());
                userPreferences.setUser(savedUser);  // Set the user for the preferences

                // Save the user preferences
                userPreferencesRepository.save(userPreferences);

                // Set the preferences for the user
                savedUser.setPreferences(userPreferences);
            }

            // Map the address if available
            if (userRequest.getAddress() != null) {
                Address address = new Address();
                address.setStreet(userRequest.getAddress().getStreet());
                address.setCity(userRequest.getAddress().getCity());
                address.setState(userRequest.getAddress().getState());
                address.setCountry(userRequest.getAddress().getCountry());
                address.setPostalCode(userRequest.getAddress().getPostalCode());
                address.setLatitude(userRequest.getAddress().getLatitude());
                address.setLongitude(userRequest.getAddress().getLongitude());
                address.setUser(savedUser);  // Set the user for the address

                // Save the address
                addressRepository.save(address);

                // Set the address for the user
                savedUser.setAddress(address);
            }

            // Map profile if available
            if (userRequest.getProfile() != null) {
                Profile profile = new Profile();
                profile.setProfilePictureUrl(userRequest.getProfile().getProfilePictureUrl());
                profile.setSelfEmployed(userRequest.getProfile().isSelfEmployed());
                profile.setCompanyType(userRequest.getProfile().getCompanyType());
                profile.setActivityType(userRequest.getProfile().getActivityType());
                profile.setUser(savedUser);  // Set the user for the profile

                // Save the profile
                profileRepository.save(profile);

                // Set the profile for the user
                savedUser.setProfile(profile);
            }

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
            responseDTO.setIsActive(savedUser.getActive());
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
            return ResponseEntity.ok(responseDTO);


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


    // Helper method for error response
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", message,
                        "path", "/api/user/createDTO"
                )
        );
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
}