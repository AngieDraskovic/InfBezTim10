package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.*;
import com.example.InfBezTim10.model.Authority;
import com.example.InfBezTim10.model.AuthorityEnum;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.IAuthorityService;
import com.example.InfBezTim10.service.IUserService;
import com.example.InfBezTim10.service.implementation.UserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IAuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil, IAuthorityService authorityService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authorityService = authorityService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(value = "/login",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserCredentialsDTO userCredentialDTO)
    {
        try {
            var authentication = authenticationManager.authenticate (
                    new UsernamePasswordAuthenticationToken(userCredentialDTO.getEmail(),
                            userCredentialDTO.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtil.generateToken(authentication);
            AuthTokenDTO tokenDTO = new AuthTokenDTO(token, token);
            return new ResponseEntity<>(tokenDTO, HttpStatus.OK);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO( "Wrong username or password!"));
        }
    }

    @PostMapping(value="/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO userDTO){
        if(userService.findByEmail(userDTO.getEmail())!=null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessageDTO( "User with given email already exists!"));
        }
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setSurname(userDTO.getSurname());
        user.setTelephoneNumber(userDTO.getTelephoneNumber());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        // samo se obicni korisnici mogu registrovati
        user.setAuthority(authorityService.getAuthority(AuthorityEnum.USER));
        userService.save(user);
        UserDetailsDTO userDTODetails = new UserDetailsDTO(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDTODetails);

    }
}
