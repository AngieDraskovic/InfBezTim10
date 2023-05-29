package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.dto.auth.*;
import com.example.InfBezTim10.dto.user.*;
import com.example.InfBezTim10.exception.*;
import com.example.InfBezTim10.exception.auth.PasswordExpiredException;
import com.example.InfBezTim10.exception.auth.TwoFactorCodeNotFoundException;
import com.example.InfBezTim10.exception.user.*;
import com.example.InfBezTim10.mapper.UserMapper;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.accountManagement.*;
import com.example.InfBezTim10.service.userManagement.*;
import com.example.InfBezTim10.service.userManagement.implementation.RecaptchaService;
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
        userService.checkPasswordExpiration(userCredentialsDTO.getEmail());

        String temporaryToken = UUID.randomUUID().toString();
        temporaryTokenService.storeTemporaryToken(userCredentialsDTO.getEmail(), temporaryToken);

        return ResponseEntity.ok(new LoginResponseDTO("Valid credentials", temporaryToken));
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

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendAuthCode(@Valid @RequestBody UserCredentialsDTO userCredentialDTO, @RequestParam String confirmationMethod, HttpSession session) {
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
        } catch (PasswordExpiredException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        }
    }

    @PostMapping(value = "/login/{email}/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@PathVariable("code") String code, @PathVariable("email") String email, HttpSession session) {
        try {
            twoFactorAuthenticationService.verifyCode(email, code);
            var authentication = (Authentication) session.getAttribute("authentication");
            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (TwoFactorCodeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IncorrectCodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
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
    // FOR TESTING PURPOSES ONLY 
    @PostMapping(value = "/login2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserCredentialsDTO userCredentialDTO) {
        try{
        //recaptchaService.isResponseValid(userCredentialDTO.getRecaptchaToken());
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(),
                        userCredentialDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        userService.isUserVerified(userCredentialDTO.getEmail());

        String token = jwtUtil.generateToken(authentication);
        AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
        return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch(NotValidRecaptchaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        }
    }


    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendAuthCode(@Valid @RequestBody UserCredentialsDTO userCredentialDTO, @RequestParam String confirmationMethod, HttpSession session) {
        try {
            recaptchaService.isResponseValid(userCredentialDTO.getRecaptchaToken());

            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(), userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            userService.isUserVerified(userCredentialDTO.getEmail());
            userService.checkPasswordExpiration(userCredentialDTO.getEmail());

            twoFactorAuthenticationService.sendCode(userCredentialDTO.getEmail(), confirmationMethod);
            session.setAttribute("authentication", authentication);

            return ResponseEntity.ok().body(new ResponseMessageDTO("Verification code sent. Please enter the code to complete the login."));
        } catch (PasswordExpiredException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO("Wrong username or password!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch(NotValidRecaptchaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/login/{email}/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@PathVariable("code") String code, @PathVariable("email") String email, HttpSession session) {
        try {
            twoFactorAuthenticationService.verifyCode(email, code);
            var authentication = (Authentication) session.getAttribute("authentication");
            session.invalidate();
            System.out.println(authentication);
            System.out.println(authentication.getPrincipal());
            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (TwoFactorCodeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (IncorrectCodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO, @RequestParam String confirmationMethod) {
        try {
            recaptchaService.isResponseValid(userRegistrationDTO.getRecaptchaToken());
            User user = userRegistrationService.registerUser(UserMapper.INSTANCE.userRegistrationDTOtoUser(userRegistrationDTO), confirmationMethod);
            return ResponseEntity.status(HttpStatus.OK).body(UserMapper.INSTANCE.userToUserDetailsDTO(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch(NotValidRecaptchaException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO(e.getMessage()));
        }
    }


    @PostMapping(value = "/activate")
    public ResponseEntity<?> activateUser(@RequestBody ActivationDTO activationDTO) {
        try {
            recaptchaService.isResponseValid(activationDTO.getRecaptchaToken());
            userActivationService.activate(activationDTO.getActivationId());
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
        } catch (UserNotFoundException | PasswordResetNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessageDTO(e.getMessage()));
        } catch (PasswordDoNotMatchException | PreviousPasswordException | IncorrectCodeException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping(value = "/renewPassword/{email}")
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
