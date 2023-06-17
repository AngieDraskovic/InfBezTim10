package com.example.InfBezTim10.service.userManagement.implementation;

import com.example.InfBezTim10.exception.CustomException;
import com.example.InfBezTim10.exception.user.IncorrectCodeException;
import com.example.InfBezTim10.exception.user.NotValidRecaptchaException;
import com.example.InfBezTim10.service.userManagement.IRecaptchaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class RecaptchaService implements IRecaptchaService {

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;
    private static final Logger logger = LoggerFactory.getLogger(RecaptchaService.class);

    @Override
    public void isResponseValid(String response) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("secret", recaptchaSecret);
        map.add("response", response);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> recaptchaResponseEntity =
                restTemplate.postForEntity(GOOGLE_RECAPTCHA_VERIFY_URL, request, String.class);
        String recaptchaResponseBody = recaptchaResponseEntity.getBody();
        logger.debug("reCAPTCHA API response: {}", recaptchaResponseBody);
        if(!isSuccessfulResponse(recaptchaResponseEntity.getBody())){
            logger.warn("Invalid reCAPTCHA token received.");
            throw new NotValidRecaptchaException("Invalid reCAPTCHA token!");
        }
    }

    public boolean isSuccessfulResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            logger.debug("reCAPTCHA JSON response: {}", jsonNode);
            return jsonNode.has("success") && jsonNode.get("success").asBoolean();
        } catch (Exception e) {
            logger.error("Error occurred while parsing reCAPTCHA response: {}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
    }
}
