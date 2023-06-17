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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


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
        logger.info("Attempting to authenticate user: {}", userCredentialsDTO.getEmail());
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userCredentialsDTO.getEmail(),
                userCredentialsDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("User {} authenticated successfully", userCredentialsDTO.getEmail());
        userService.isUserVerified(userCredentialsDTO.getEmail());
        logger.info("Recaptcha verification and user verification completed for user: {}", userCredentialsDTO.getEmail());
        recaptchaService.isResponseValid(userCredentialsDTO.getRecaptchaToken());

        String temporaryToken = UUID.randomUUID().toString();
        temporaryTokenService.storeTemporaryToken(userCredentialsDTO.getEmail(), temporaryToken);
        if (userService.isPasswordExpired(userCredentialsDTO.getEmail())) {
            logger.warn("Password expired for user: {}", userCredentialsDTO.getEmail());
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
        logger.info("2FA code sent to user: {}", twoFAMethodRequestDTO.getEmail());
        return ResponseEntity.ok("2FA code sent");
    }

    @PostMapping(value = "/2fa/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify2FA(@RequestBody VerificationRequestDTO verificationRequestDTO) {
        logger.info("Received 2FA verification request for user: {}", verificationRequestDTO.getEmail());
        User user = userService.findByEmail(verificationRequestDTO.getEmail());
        if (!temporaryTokenService.isValidTemporaryToken(verificationRequestDTO.getEmail(), verificationRequestDTO.getTemporaryToken())) {
            logger.warn("Invalid or expired temporary token for user: {}", verificationRequestDTO.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired temporary token");
        }

        twoFactorAuthenticationService.verifyCode(verificationRequestDTO.getEmail(), verificationRequestDTO.getCode());
        temporaryTokenService.removeTemporaryToken(verificationRequestDTO.getEmail());
        logger.info("2FA verification successful for user: {}", verificationRequestDTO.getEmail());
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                Collections.singletonList(user.getAuthority()));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(user.getAuthority()));
        String jwt = jwtUtil.generateToken(auth);
        AuthTokenDTO tokenDTO = new AuthTokenDTO(jwt, jwt);
        return ResponseEntity.ok(tokenDTO);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        logger.info("Received request to check email: {}", email);
        boolean exists = userService.emailExists(email);
        logger.info("Email existence result: {}", exists);
        return ResponseEntity.ok(exists);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO, @RequestParam String confirmationMethod) {
        logger.info("Received request to register user with email: {}", userRegistrationDTO.getEmail());
        try {
            recaptchaService.isResponseValid(userRegistrationDTO.getRecaptchaToken());
            User user = userRegistrationService.registerUser(UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO), confirmationMethod);
            logger.info("User registration succeeded for email: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(UserMapper.INSTANCE.userToUserDetailsDTO(user));
        } catch (IOException e) {
            logger.error("Exception during user registration");
            throw new RuntimeException(e);
        } catch (NotValidRecaptchaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody ActivationDTO activationDTO) {
        logger.info("Received request to activate user with activationId: {}", activationDTO.getActivationId());
        try {
            userActivationService.activate(activationDTO.getActivationId());
            logger.info("User activation succeeded for activationId: {}", activationDTO.getActivationId());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDTO("Successful account activation!"));
        } catch (NotFoundException e) {
            logger.error("Activation failed for activationId: {}", activationDTO.getActivationId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @GetMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> sendEmailForPasswordReset(@PathVariable("email") String email, @RequestParam String confirmationMethod) {
        logger.info("Received request to send password reset email to: {}", email);
        try {
            passwordResetService.sendEmail(email, confirmationMethod);
            logger.info("Password reset email sent to: {}", email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            logger.warn("Error during password reset for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> resetPassword(@Valid @PathVariable("email") String email, @RequestBody ResetPasswordDTO passwordDTO) {
        logger.info("Received request to reset password for email: {}", email);
        try {
            this.recaptchaService.isResponseValid(passwordDTO.getRecaptchaToken());
            passwordResetService.resetPassword(email, passwordDTO);
            logger.info("Password reset successful for email: {}", email);
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
        logger.info("Received request to renew password for email: {}", email);
        try {
            if (!temporaryTokenService.isValidTemporaryToken(email, passwordDTO.getTemporaryToken())) {
                logger.warn("Invalid or expired temporary token for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired temporary token");
            }
            passwordResetService.renewPassword(email, passwordDTO);
            logger.info("Password renewal successful for email: {}", email);
            temporaryTokenService.removeTemporaryToken(email);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | PreviousPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }
}
