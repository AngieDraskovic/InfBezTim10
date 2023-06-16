package com.example.InfBezTim10.controller;

import com.example.InfBezTim10.dto.auth.AuthTokenDTO;
import com.example.InfBezTim10.dto.auth.OauthTokenDTO;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserData;
import com.example.InfBezTim10.security.JwtUtil;
import com.example.InfBezTim10.service.accountManagement.IOAuthService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;

@RestController
@RequestMapping("/api/oauth")
public class OauthController {

    @Value("${google.clientId}")
    String googleClientId;

    private final IOAuthService oAuthService;

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public OauthController(IOAuthService oAuthService, JwtUtil jwtUtil) {
        this.oAuthService = oAuthService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(value = "/google")
    public ResponseEntity<AuthTokenDTO> google(@RequestBody OauthTokenDTO oauthTokenDTO) throws IOException {
        logger.info("Received request for Google OAuth");
        final NetHttpTransport transport = new NetHttpTransport();
        final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder verifier =
                new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(Collections.singletonList(googleClientId));
        final GoogleIdToken googleIdToken = GoogleIdToken.parse(verifier.getJsonFactory(), oauthTokenDTO.getToken());
        final GoogleIdToken.Payload payload = googleIdToken.getPayload();

        UserData userData = new UserData(
                (String) payload.get("given_name"),
                (String) payload.get("family_name"),
                payload.getEmail(),
                null,
                null
        );
        User loggedUser = oAuthService.processOAuthUser(payload.getSubject(), userData);
        logger.info("OAuth user processed");
        AuthTokenDTO tokenDTO = login(loggedUser);

        return ResponseEntity.ok().body(tokenDTO);
    }

    private AuthTokenDTO login(User loggedUser) {
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(loggedUser.getEmail(), loggedUser.getPassword(),
                Collections.singletonList(loggedUser.getAuthority()));
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(loggedUser.getAuthority()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String token = jwtUtil.generateToken(auth);
        logger.info("Login successful for user: {}", loggedUser.getEmail());
        return new AuthTokenDTO(token, token);
    }
}
