package com.example.InfBezTim10.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    private HttpStatus status;
    private String message;
}
