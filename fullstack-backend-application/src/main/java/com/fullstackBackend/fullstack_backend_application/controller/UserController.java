package com.fullstackBackend.fullstack_backend_application.controller;

import com.fullstackBackend.fullstack_backend_application.model.User;
import com.fullstackBackend.fullstack_backend_application.repository.UserRepository;
import com.fullstackBackend.fullstack_backend_application.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    // CRUD Operations
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User updatedUser = user.get();
            updatedUser.setName(userDetails.getName());
            updatedUser.setEmail(userDetails.getEmail());
            updatedUser.setPassword(passwordEncoder.encode(userDetails.getPassword())); // Hash the password
            return ResponseEntity.ok(userRepository.save(updatedUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Login with OTP
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String email = loginUser.getEmail();
        String password = loginUser.getPassword();

        System.out.println("Login request received for email: " + email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            System.out.println("User found: " + user.getEmail());

            if (passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Password matches for user: " + user.getEmail());

                // Generate OTP
                String otp = generateOTP();
                System.out.println("Generated OTP: " + otp);

                // Send OTP to the user's email
                emailService.sendOTP(email, otp);

                // Save the OTP in the database
                user.setOtp(otp);
                userRepository.save(user);
                System.out.println("OTP saved for user: " + user.getEmail());

                Map<String, String> response = new HashMap<>();
                response.put("message", "OTP sent to your email. Please verify.");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Invalid password for user: " + user.getEmail());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid email or password!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } else {
            System.out.println("User not found for email: " + email);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String email) {
        String otp = generateOTP();
        emailService.sendOTP(email, otp);
        return ResponseEntity.ok("Test email sent to " + email);
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (otp.equals(user.getOtp())) {
                // OTP is valid
                Map<String, String> response = new HashMap<>();
                response.put("message", "OTP verified successfully!");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid OTP!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "User not found!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Helper method to generate a 6-digit OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}