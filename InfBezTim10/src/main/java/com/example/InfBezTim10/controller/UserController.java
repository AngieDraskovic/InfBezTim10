package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.dto.auth.*;
import com.example.InfBezTim10.dto.user.*;
import com.example.InfBezTim10.exception.*;
import com.example.InfBezTim10.exception.user.*;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.accountManagement.*;
import com.example.InfBezTim10.service.userManagement.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.UUID;


@RestController
@RequestMapping("/api/user")
public class UserController {
    private final IUserService userService;
    private final ITemporaryTokenService temporaryTokenService;
    private final IUserRegistrationService userRegistrationService;
    private final IUserActivationService userActivationService;
    private final IPasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;
    private final IRecaptchaService recaptchaService;
    private final ITwoFactorAuthenticationService twoFactorAuthenticationService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(IUserService userService, ITemporaryTokenService temporaryTokenService, IUserRegistrationService userRegistrationService,
                          IUserActivationService userActivationService, IPasswordResetService passwordResetService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          ITwoFactorAuthenticationService twoFactorAuthenticationService, IRecaptchaService recaptchaService) {
        this.userService = userService;
        this.temporaryTokenService = temporaryTokenService;
        this.userRegistrationService = userRegistrationService;
        this.userActivationService = userActivationService;
        this.passwordResetService = passwordResetService;
        this.authenticationManager = authenticationManager;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
        this.jwtUtil = jwtUtil;
        this.recaptchaService = recaptchaService;
    }


    @GetMapping(value = "/me")
    public ResponseEntity<UserMeDTO> userDTOResponseEntity(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        UserMeDTO userMeDTO = new UserMeDTO(user);
        return new ResponseEntity<>(userMeDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UserCredentialsDTO userCredentialsDTO) {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userCredentialsDTO.getEmail(),
                userCredentialsDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        userService.isUserVerified(userCredentialsDTO.getEmail());
        recaptchaService.isResponseValid(userCredentialsDTO.getRecaptchaToken());

        String temporaryToken = UUID.randomUUID().toString();
        temporaryTokenService.storeTemporaryToken(userCredentialsDTO.getEmail(), temporaryToken);
        if (userService.isPasswordExpired(userCredentialsDTO.getEmail())) {
            return ResponseEntity.ok(new LoginResponseDTO("Password expired", true, temporaryToken));
        }

        return ResponseEntity.ok(new LoginResponseDTO("Valid credentials", false, temporaryToken));
    }

    @PostMapping(value = "/2fa/method", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> select2FAMethod(@RequestBody TwoFAMethodRequestDTO twoFAMethodRequestDTO) {
        userService.findByEmail(twoFAMethodRequestDTO.getEmail());
        if (!twoFAMethodRequestDTO.getMethod().equalsIgnoreCase("email") && !twoFAMethodRequestDTO.getMethod().equalsIgnoreCase("sms")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid method type");
        }

        twoFactorAuthenticationService.sendCode(twoFAMethodRequestDTO.getEmail(), twoFAMethodRequestDTO.getMethod());
        return ResponseEntity.ok("2FA code sent");
    }

    @PostMapping(value = "/2fa/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify2FA(@RequestBody VerificationRequestDTO verificationRequestDTO) {
        User user = userService.findByEmail(verificationRequestDTO.getEmail());
        if (!temporaryTokenService.isValidTemporaryToken(verificationRequestDTO.getEmail(), verificationRequestDTO.getTemporaryToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired temporary token");
        }

        twoFactorAuthenticationService.verifyCode(verificationRequestDTO.getEmail(), verificationRequestDTO.getCode());
        temporaryTokenService.removeTemporaryToken(verificationRequestDTO.getEmail());

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(user.getAuthority()));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(user.getAuthority()));
        String jwt = jwtUtil.generateToken(auth);
        AuthTokenDTO tokenDTO = new AuthTokenDTO(jwt, jwt);
        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO, @RequestParam String confirmationMethod) {
        try {
            recaptchaService.isResponseValid(userRegistrationDTO.getRecaptchaToken());
            User user = userRegistrationService.registerUser(UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO), confirmationMethod);
            return ResponseEntity.status(HttpStatus.OK).body(UserMapper.INSTANCE.userToUserDetailsDTO(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NotValidRecaptchaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody ActivationDTO activationDTO) {
        try {
            userActivationService.activate(activationDTO.getActivationId());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDTO("Successful account activation!"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @GetMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> sendEmailForPasswordReset(@PathVariable("email") String email, @RequestParam String confirmationMethod) {
        try {
            passwordResetService.sendEmail(email, confirmationMethod);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> resetPassword(@Valid @PathVariable("email") String email, @RequestBody ResetPasswordDTO passwordDTO) {
        try {
            this.recaptchaService.isResponseValid(passwordDTO.getRecaptchaToken());
            passwordResetService.resetPassword(email, passwordDTO);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException | PasswordResetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PreviousPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | IncorrectCodeException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value = "/renewPassword/{email}")
    public ResponseEntity<?> renewPassword(@Valid @PathVariable("email") String email, @RequestBody RenewPasswordDTO passwordDTO) {
        try {
            if (!temporaryTokenService.isValidTemporaryToken(email, passwordDTO.getTemporaryToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired temporary token");
            }
            passwordResetService.renewPassword(email, passwordDTO);
            temporaryTokenService.removeTemporaryToken(email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | PreviousPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }
}
