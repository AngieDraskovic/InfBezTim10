package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.dto.user.*;
import com.example.InfBezTim10.exception.*;
import com.example.InfBezTim10.exception.user.*;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.accountManagement.*;
import com.example.InfBezTim10.service.userManagement.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.security.Principal;


@RestController
@RequestMapping("/api/user")
public class UserController {
    private final IUserService userService;
    private final IUserRegistrationService userRegistrationService;
    private final IUserActivationService userActivationService;
    private final IPasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;


    private final ITwoFactorAuthenticationService twoFactorAuthenticationService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(IUserService userService, IUserRegistrationService userRegistrationService,
                          IUserActivationService userActivationService, IPasswordResetService passwordResetService,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil, ITwoFactorAuthenticationService twoFactorAuthenticationService) {
        this.userService = userService;
        this.userRegistrationService = userRegistrationService;
        this.userActivationService = userActivationService;
        this.passwordResetService = passwordResetService;
        this.authenticationManager = authenticationManager;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping(value = "/me")
    public ResponseEntity<UserMeDTO> userDTOResponseEntity(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        UserMeDTO userMeDTO = new UserMeDTO(user);
        return new ResponseEntity<>(userMeDTO, HttpStatus.OK);
    }


    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendAuthCode(@Valid @RequestBody UserCredentialsDTO userCredentialDTO,  @RequestParam String confirmationMethod, HttpSession session) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(), userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            userService.isUserVerified(userCredentialDTO.getEmail());
            userService.checkPasswordExpiration(userCredentialDTO.getEmail());


            twoFactorAuthenticationService.sendCode(userCredentialDTO.getEmail(), confirmationMethod);

            session.setAttribute("authentication", authentication);

            return ResponseEntity.ok().body(new ResponseMessageDTO("Verification code sent. Please enter the code to complete the login."));
        }catch(PasswordExpiredException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        }catch (AuthenticationException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/login/{email}/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@PathVariable("code") String code, @PathVariable("email") String email, HttpSession session) {
        try {
            twoFactorAuthenticationService.verifyCode(email, code);
            var authentication = (Authentication) session.getAttribute("authentication");
            System.out.println(authentication);
            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        }catch(TwoFactorCodeNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        }catch(IncorrectCodeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO, @RequestParam String confirmationMethod
    ) {
        try {
            User user = userRegistrationService.registerUser(UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO), confirmationMethod);
            return ResponseEntity.status(HttpStatus.OK).body(UserMapper.INSTANCE.userToUserDetailsDTO(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping(value = "/activate/{activationId}")
    public ResponseEntity<?> activateUser(@PathVariable("activationId") String activationId) {
        try {
            userActivationService.activate(activationId);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessageDTO("Successful account activation!"));
    }

    @GetMapping(value = "/resetPassword/{email}")
    public ResponseEntity<?> sendEmailForPasswordReset(@PathVariable("email") String email, @RequestParam String confirmationMethod) {
        try {
            passwordResetService.sendEmail(email, confirmationMethod);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
            passwordResetService.resetPassword(email, passwordDTO);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | PreviousPasswordException  | IncorrectCodeException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value="/renewPassword/{email}")
    public ResponseEntity<?> renewPassword(@Valid @PathVariable("email") String email, @RequestBody RenewPasswordDTO passwordDTO) {
        try {
            passwordResetService.renewPassword(email, passwordDTO);
            SecurityContextHolder.clearContext();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | PreviousPasswordException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }
}
