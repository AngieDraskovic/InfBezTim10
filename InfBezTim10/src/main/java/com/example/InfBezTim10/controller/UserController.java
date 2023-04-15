package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.exception.EmailAlreadyExistsException;
import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.ISendgridEmailService;
import com.example.InfBezTim10.service.IUserActivationService;
import com.example.InfBezTim10.service.IUserService;
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
    private final Random rand = new Random();

    @Autowired
    public UserController(IUserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, IUserActivationService userActivationService,
                          ISendgridEmailService sendgridEmailService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userActivationService = userActivationService;
        this.sendgridEmailService = sendgridEmailService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserCredentialsDTO userCredentialDTO) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(),
                            userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        try {
            User user = UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setActive(false);
            user = userService.register(user);
            UserDetailsDTO userDetailsDTO = UserMapper.INSTANCE.userToUserDetailsDTO(user);

            userActivationService.deleteIfAlreadyExists(user);
            ZoneOffset desiredOffset = ZoneOffset.of("+04:00");
            ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(desiredOffset);
            UserActivation activation = new UserActivation(String.valueOf(rand.nextInt(Integer.MAX_VALUE)), user,
                    zonedDateTime.toLocalDateTime());
            userActivationService.save(activation);
            sendgridEmailService.sendConfirmEmailMessage(user, activation.getActivationId());
            return ResponseEntity.status(HttpStatus.OK).body(userDetailsDTO);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
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
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDTO("Succesful account activation!"));
    }
}
