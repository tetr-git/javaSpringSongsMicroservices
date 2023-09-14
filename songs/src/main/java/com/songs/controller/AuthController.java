package com.songs.controller;

import de.htwb.ai.model.User;
import de.htwb.ai.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("songsWS-max_samuel/rest")
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
