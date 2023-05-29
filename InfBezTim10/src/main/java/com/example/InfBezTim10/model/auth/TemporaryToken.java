package com.example.InfBezTim10.model.auth;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "temporary-tokens")
@Getter
@AllArgsConstructor
@Setter
@RequiredArgsConstructor
public class TemporaryToken extends BaseEntity {
    private String email;

    private String token;

    private LocalDateTime expiryDate;
}
