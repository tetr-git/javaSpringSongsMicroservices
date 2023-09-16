package com.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("songsMS")
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository repo) {
        this.userRepo = repo;
    }

    private String currentToken;
    private String currentUserId;

    @PostMapping("/auth")
    public ResponseEntity<String> authenticateUser(@RequestBody User user) {
        String userId = user.getUserId();
        String password = user.getPassword();

        List<User> users = userRepo.authenticateUser(userId, password);
        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();        }

        currentToken = generateRandomToken();
        currentUserId = userId;
        return ResponseEntity.ok().body(currentToken);
    }

    //post route validate token
    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestBody TokenRequest tokenRequest) {
        // Check if the token is null or empty in the tokenRequest object
        if (tokenRequest == null || tokenRequest.getToken() == null || tokenRequest.getToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Compare the token from the request with your current token
        if (tokenRequest.getToken().equals(currentToken)) {
            return ResponseEntity.ok().body(null);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/getUserId")
    public ResponseEntity<String> getUserId(
            @RequestHeader("Authorization") String authorization
    ) {
        if (!isAuthorizationValid(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepo.findUserByStringId(getCurrentUserId());
        return ResponseEntity.ok().body(user.getUserId());
    }



    private static String generateRandomToken() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public boolean isAuthorizationValid(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            return false;
        }
        return authorization.equals(currentToken);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}
