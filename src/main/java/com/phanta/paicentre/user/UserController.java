package com.phanta.paicentre.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@CrossOrigin(origins = " http://localhost:5173")
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now(),
                            "status", HttpStatus.NOT_FOUND.value(),
                            "error", "Not Found",
                            "message", "User not found",
                            "path", "/api/user/" + id
                    ));
        }
        return ResponseEntity.ok(user.get());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }


    @PostMapping("/createDTO")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO userRequest) {
        ResponseEntity<Map<String, Object>> validationResponse = UserRequestValidator.validateUserRequest(userRequest);

        if (validationResponse != null) {
            // Return bad request with validation error
            return validationResponse;
        }
        return userService.createUserDTO(userRequest);
    }

//    @PutMapping("/update/{id}")
//    public ResponseEntity<Object> updateUser(@PathVariable String id, @RequestBody UserRequestDTO userRequestDTO) {
//        if (userRequestDTO == null || isEmpty(userRequestDTO)) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "timestamp", LocalDateTime.now(),
//                    "status", HttpStatus.BAD_REQUEST.value(),
//                    "error", "Bad Request",
//                    "message", "No fields to update or all fields are empty",
//                    "path", "/api/user/update/" + id
//            ));
//        }
//
//        try {
//            // Call the service to update the user
//            User updatedUser = userService.updateUser(id, userRequestDTO);
//            return ResponseEntity.ok(updatedUser);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "timestamp", LocalDateTime.now(),
//                    "status", HttpStatus.NOT_FOUND.value(),
//                    "error", "Not Found",
//                    "message", "User not found with ID: " + id,
//                    "path", "/api/user/update/" + id
//            ));
//        }
//    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Validates an email address.
     *
     * @param requestBody The object containing email details in the request body.
     * @return ResponseEntity with a JSON object containing the validation result.
     */
    @PostMapping("/email/validate")
    public ResponseEntity<Map<String, Boolean>> validateEmail(@RequestBody Object requestBody) {
        return ResponseEntity.ok(userService.validateEmail(requestBody));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}
