package com.example.InfBezTim10.controller;


import com.example.InfBezTim10.dto.AuthTokenDTO;
import com.example.InfBezTim10.dto.ResponseMessageDTO;
import com.example.InfBezTim10.dto.UserCredentialsDTO;
import com.example.InfBezTim10.security.JwtUtil;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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

}
