package com.example.InfBezTim10.exception;

import com.example.InfBezTim10.dto.ErrorDTO;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {

//    @ExceptionHandler({Exception.class})
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    protected ResponseEntity<ErrorDTO> pera(Exception e) {
//        String pera = String.valueOf(e.getClass());
//        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler({ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected ResponseEntity<ErrorDTO> expiredToken(Exception e) {
        String exceptionMessage = e.getMessage();
        ErrorDTO errorDTO = new ErrorDTO(HttpStatus.UNAUTHORIZED, exceptionMessage);
        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ErrorDTO> handleConstraintViolationException(MethodArgumentNotValidException e) {
        List<ObjectError> errorList = e.getBindingResult().getAllErrors();
        StringBuilder sb = new StringBuilder();

        for (ObjectError error : errorList) {
            FieldError fe = (FieldError) error;
            sb.append(error.getDefaultMessage());
            sb.append(System.getProperty("line.separator"));
        }
        ErrorDTO errorDTO = new ErrorDTO(HttpStatus.BAD_REQUEST, sb.toString());

        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
