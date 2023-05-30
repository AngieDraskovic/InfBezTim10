package com.example.InfBezTim10.exception;

import com.example.InfBezTim10.dto.ErrorResponseDTO;
import com.example.InfBezTim10.exception.auth.EmailAlreadyExistsException;
import com.example.InfBezTim10.exception.auth.PasswordExpiredException;
import com.example.InfBezTim10.exception.auth.TwoFactorCodeSendingException;
import com.example.InfBezTim10.exception.auth.UserNotVerifiedException;
import com.example.InfBezTim10.exception.user.IncorrectCodeException;
import com.example.InfBezTim10.exception.user.PreviousPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;

@RestControllerAdvice


public class AuthenticationExceptionHandler {
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
    public ResponseEntity<ErrorResponseDTO> handleUserNotVerifiedException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({PasswordExpiredException.class})
    public ResponseEntity<ErrorResponseDTO> handlePasswordExpiredException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({PreviousPasswordException.class})
    public ResponseEntity<ErrorResponseDTO> handlePreviousPasswordException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({TwoFactorCodeSendingException.class})
    public ResponseEntity<ErrorResponseDTO> handleErrorSendingTwoFactorCode(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({IncorrectCodeException.class})
    public ResponseEntity<ErrorResponseDTO> handleIncorrectCodeException(Exception exception) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
