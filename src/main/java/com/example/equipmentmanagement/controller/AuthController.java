package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.dto.*;
import com.example.equipmentmanagement.entity.ERole;
import com.example.equipmentmanagement.entity.Role;
import com.example.equipmentmanagement.entity.User;
import com.example.equipmentmanagement.jwt.AuthTokenFilter;
import com.example.equipmentmanagement.jwt.JwtUtils;
import com.example.equipmentmanagement.repository.RoleRepository;
import com.example.equipmentmanagement.repository.UserRepository;
import com.example.equipmentmanagement.service.UserDetailsImpl;
import com.example.equipmentmanagement.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    AuthTokenFilter authTokenFilter;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error: Username is already taken!",
                            null
                    ));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Error: Email is already in use!",
                            null
                    ));
        }
        // Create new user's account
        User user = new User();
        user.setId("user-" + new Timestamp(System.currentTimeMillis()).getTime());
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setAddress(signUpRequest.getAddress());
        user.setPhone(signUpRequest.getPhone());
        user.set_active(signUpRequest.getActive());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
        strRoles.forEach(role -> {
            if ("admin".equals(role)) {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);
            } else {
                Role userRole = roleRepository.findByName(ERole.ROLE_MAINTAINER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            }
        });
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(
                HttpStatus.OK.value(),
                "User registered successfully!",
                null
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        JWTResponse jwtResponse = new JWTResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getActive(),
                roles);
        return ResponseEntity.ok(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        "Login successful!",
                        jwtResponse
                ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        String jwt = authTokenFilter.parseJwt(request);
        if (jwt == null || !jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Invalid access token",
                            null
                    ));
        }
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        ProfileResponse profileRes = new ProfileResponse(userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), userDetails.getPhone(), userDetails.getAddress(), userDetails.getActive(), roles);
        return ResponseEntity.ok(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        "Get profile successful!",
                        profileRes
                ));
    }
}
