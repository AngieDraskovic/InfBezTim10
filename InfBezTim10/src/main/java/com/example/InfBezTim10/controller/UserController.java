package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.exception.*;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.PasswordReset;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;


@RestController
@RequestMapping("/api/user")
public class UserController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final IUserActivationService userActivationService;
    private final ISendgridEmailService sendgridEmailService;
    private final IPasswordResetService passwordResetService;
    private final ITwillioService twillioService;
    private final Random rand = new Random();

    @Autowired
    public UserController(IUserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, IUserActivationService userActivationService,
                          ISendgridEmailService sendgridEmailService, IPasswordResetService passwordResetService, ITwillioService twillioService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userActivationService = userActivationService;
        this.sendgridEmailService = sendgridEmailService;
        this.passwordResetService = passwordResetService;
        this.twillioService = twillioService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserCredentialsDTO userCredentialDTO) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(),
                            userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findByEmail(userCredentialDTO.getEmail());
            if(!user.getActive()){
                throw new UserNotActivatedException("You have not confirmed your email/phone when registering");
            }

            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        } catch (UserNotActivatedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO,  @RequestParam String confirmationMethod
    ) {
        try {
            User user = UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setActive(false);
            user = userService.register(user);
            UserDetailsDTO userDetailsDTO = UserMapper.INSTANCE.userToUserDetailsDTO(user);

            UserActivation activation = userActivationService.create(user);

            if (confirmationMethod.equalsIgnoreCase("email")) {
                sendgridEmailService.sendConfirmEmailMessage(user, activation.getActivationId());
            } else if (confirmationMethod.equalsIgnoreCase("sms")) {
                twillioService.sendConfirmNumberSMS(user, activation.getActivationId());
            }
            return ResponseEntity.status(HttpStatus.OK).body(userDetailsDTO);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping(value="/activate/{activationId}")
    public ResponseEntity<?> activateUser(@PathVariable("activationId") String activationId){
        try {
            userActivationService.activate(activationId);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDTO("Successful account activation!"));
    }

    @GetMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> sendEmailForPasswordReset(@PathVariable("email") String email, @RequestParam String confirmationMethod){
        try {
            User user = userService.findByEmail(email);
            passwordResetService.deleteIfAlreadyExists(user);
            PasswordReset reset = new PasswordReset(String.valueOf(rand.nextInt(Integer.MAX_VALUE)), user, LocalDateTime.now());
            passwordResetService.save(reset);
            if (confirmationMethod.equalsIgnoreCase("email")) {
                sendgridEmailService.sendNewPasswordMail(user, reset.getCode());
            } else if (confirmationMethod.equalsIgnoreCase("sms")) {
                twillioService.sendResetPasswordSMS(user, reset.getCode());
            }

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> resetPassword(@Valid @PathVariable("email") String email, @RequestBody ResetPasswordDTO passwordDTO){
        try {
            User user = userService.findByEmail(email);
            passwordResetService.resetPassword(user, passwordDTO);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch(UserNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException e) {
            throw new RuntimeException(e);
        }
    }


}
