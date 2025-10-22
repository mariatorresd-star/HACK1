package com.example.hack1base.User.application;

import com.example.hack1base.User.domain.User;
import com.example.hack1base.User.domain.UserResponse;
import com.example.hack1base.User.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------- AUTH ----------
    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponse> login(@RequestBody User request) {
        return ResponseEntity.ok(userService.login(request.getEmail(), request.getPassword()));
    }

    // ---------- USERS ----------
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
