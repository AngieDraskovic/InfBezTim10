package com.example.InfBezTim10.exception;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.util.List;


@ControllerAdvice
public class CustomExceptionHandler{

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ErrorDTO> handleConstraintViolationException(MethodArgumentNotValidException e) {
        List<ObjectError> errorList = e.getBindingResult().getAllErrors();
        StringBuilder sb = new StringBuilder();

        for (ObjectError error : errorList ) {
            FieldError fe = (FieldError) error;
            sb.append(error.getDefaultMessage());
            sb.append(System.getProperty("line.separator"));
        }
        ErrorDTO errorDTO = new ErrorDTO(HttpStatus.BAD_REQUEST, sb.toString());

        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}