package com.phanta.paicentre.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phanta.paicentre.oauthToken.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Used for JSON parsing
    private final JwtTokenUtil jwtTokenUtil;

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



    public ResponseEntity<?> createUserDTO(UserRequestDTO userRequest) {
        // Check if the email already exists
        Optional<User> existingUser = userRepository.findByEmail(userRequest.getPersonal().getEmail());
        if (existingUser.isPresent()) {
            // Return a bad request response with the existing user's ID
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", "User already exists",
                            "userId", "USR_" + existingUser.get().getId()
                    )
            );
        }

        // Map the personal info from the DTO to the User object
        User user = new User();
        user.setFirstName(userRequest.getPersonal().getFirstName());
        user.setLastName(userRequest.getPersonal().getLastName());
        user.setEmail(userRequest.getPersonal().getEmail());
        user.setPhoneNumber(userRequest.getPersonal().getPhone());
        user.setPassword(userRequest.getPersonal().getPassword());
        user.setSex(userRequest.getPersonal().getSex());
        user.setDateOfBirth(LocalDate.of(2000, 02, 02));

        // Save the new user
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }


    public User updateUser(String id, User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstName(userDetails.getFirstName());
                    existingUser.setLastName(userDetails.getLastName());
                    existingUser.setEmail(userDetails.getEmail());
                    existingUser.setPhoneNumber(userDetails.getPhoneNumber());
                    existingUser.setPassword(userDetails.getPassword());
                    existingUser.setEmailVerified(userDetails.getEmailVerified());
                    existingUser.setIsActive(userDetails.getIsActive());
                    existingUser.setUpdatedAt(userDetails.getUpdatedAt());
                    //existingUser.setDateOfBirth(userDetails.getDateOfBirth());
                    //existingUser.setSex(userDetails.getSex());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
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