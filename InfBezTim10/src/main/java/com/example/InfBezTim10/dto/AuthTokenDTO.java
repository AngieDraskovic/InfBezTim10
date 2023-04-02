package com.example.InfBezTim10.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthTokenDTO {
    String accessToken;
    String refreshToken;
}