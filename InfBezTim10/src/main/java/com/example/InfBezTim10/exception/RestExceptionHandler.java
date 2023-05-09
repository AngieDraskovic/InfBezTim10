package com.example.InfBezTim10.exception;

import com.example.InfBezTim10.dto.ErrorResponseDTO;
import com.example.InfBezTim10.exception.certificate.CertificateNotFoundException;
import com.example.InfBezTim10.exception.certificate.CertificateValidationException;
import com.example.InfBezTim10.exception.certificateRequest.CertificateRequestValidationException;
import com.example.InfBezTim10.exception.user.EmailAlreadyExistsException;
import com.example.InfBezTim10.exception.user.UserNotVerifiedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({AuthenticationException.class, AuthenticationServiceException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                "Email or password is incorrect.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({UserNotVerifiedException.class})
    public ResponseEntity<ErrorResponseDTO> handleUserNotActivatedException(UserNotVerifiedException exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                "Your account is not verified.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({CertificateNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleCertificateNotFoundException(CertificateNotFoundException exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({CertificateValidationException.class})
    public ResponseEntity<ErrorResponseDTO> handleCertificationValidationException(CertificateNotFoundException exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({CertificateRequestValidationException.class})
    public ResponseEntity<ErrorResponseDTO> handleCertificateRequestValidationException(CertificateRequestValidationException exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
