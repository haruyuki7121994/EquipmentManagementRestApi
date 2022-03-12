package com.example.equipmentmanagement.controller;

import com.example.equipmentmanagement.aws.AmazonClient;
import com.example.equipmentmanagement.dto.*;
import com.example.equipmentmanagement.entity.Code;
import com.example.equipmentmanagement.entity.ERole;
import com.example.equipmentmanagement.entity.Role;
import com.example.equipmentmanagement.entity.User;
import com.example.equipmentmanagement.jwt.AuthTokenFilter;
import com.example.equipmentmanagement.jwt.JwtUtils;
import com.example.equipmentmanagement.repository.CodeRepository;
import com.example.equipmentmanagement.repository.RoleRepository;
import com.example.equipmentmanagement.repository.UserRepository;
import com.example.equipmentmanagement.service.EmailSenderService;
import com.example.equipmentmanagement.service.ResponseImpl;
import com.example.equipmentmanagement.service.UserDetailsImpl;
import com.example.equipmentmanagement.service.UserDetailsServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    ResponseImpl responseService;
    @Autowired
    CodeRepository codeRepository;
    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    private AmazonClient amazonClient;

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
            switch (role) {
                case "admin": {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                    break;
                }
                case "maintainer": {
                    Role userRole = roleRepository.findByName(ERole.ROLE_MAINTAINER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                    break;
                }
                case "guest": {
                    Role guestRole = roleRepository.findByName(ERole.ROLE_GUEST)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(guestRole);
                    break;
                }
                default: break;
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
                userDetails.getEmail(), userDetails.getPhone(), userDetails.getAddress(),
                userDetails.getAvatar(), userDetails.getActive(), roles);
        return ResponseEntity.ok(
                new MessageResponse(
                        HttpStatus.OK.value(),
                        "Get profile successful!",
                        profileRes
                ));
    }

    @PostMapping("/edit/profile")
    public ResponseEntity<?> changeProfile(HttpServletRequest request, @Valid @RequestBody UserRequest userRequest) {
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
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "User not found",
                                null
                        )
                );
            }
            User user = userOptional.get();
            user.setAddress(userRequest.getAddress());
            user.setPhone(userRequest.getPhone());
            userRepository.save(user);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Update user successful!",
                            null
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/edit/password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @Valid @RequestBody UserRequest userRequest) {
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
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "User not found",
                                null
                        )
                );
            }
            if (!Objects.equals(userRequest.getPassword(), userRequest.getRePassword())) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Password not match!",
                                null
                        )
                );
            }
            User user = userOptional.get();
            user.setPassword(encoder.encode(userRequest.getPassword()));
            userRepository.save(user);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Update password successful!",
                            null
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/edit/avatar")
    public ResponseEntity<?> changeAvatar(HttpServletRequest request, @RequestPart(value = "file") MultipartFile file) {
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
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "User not found",
                                null
                        )
                );
            }
            User user = userOptional.get();
            user.setAvatar(amazonClient.uploadFile(file));
            userRepository.save(user);
            return ResponseEntity.ok(
                    new MessageResponse(
                            HttpStatus.OK.value(),
                            "Update avatar successful!",
                            null
                    )
            );
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/forgot-password/send")
    public ResponseEntity<?> sendEmailForgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return responseService.badRequest("User not found!");
        }
        try {
            User user = userOptional.get();
            Code code = new Code();
            String randomCode = RandomStringUtils.randomNumeric(6);
            code.setCode(randomCode);
            code.setUser(user);
            code.setUsed(false);
            codeRepository.save(code);
            emailSenderService.sendEmail(forgotPasswordRequest.getEmail(), "Verify Forgot Password Email", randomCode);
            return responseService.success("Send code successful!", code);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        Optional<User> userOptional = userRepository.findByEmail(forgotPasswordRequest.getEmail());
        if (userOptional.isEmpty()) {
            return responseService.badRequest("User not found!");
        }
        try {
            User user = userOptional.get();
            Optional<Code> codeOptional = codeRepository.findTopByUserAndCodeAndUsedIsFalseOrderByIdDesc(user, forgotPasswordRequest.getCode());
            if (codeOptional.isEmpty()) {
                return responseService.badRequest("Verify Failed! Invalid code!");
            }
            if (!Objects.equals(forgotPasswordRequest.getNewPassword(), forgotPasswordRequest.getRePassword())) {
                return responseService.badRequest("Password not match!");
            }
            user.setPassword(encoder.encode(forgotPasswordRequest.getNewPassword()));
            userRepository.save(user);

            Code code = codeOptional.get();
            code.setUsed(true);
            codeRepository.save(code);

            return responseService.success("Verified Successful!", code);
        } catch (Exception e) {
            return responseService.badRequest(e.getMessage());
        }
    }
}
