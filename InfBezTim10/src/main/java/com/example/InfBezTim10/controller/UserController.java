package com.example.InfBezTim10.controller;


import com.example.InfBezTim10.service.UserServiceJPA;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {


    @Autowired
    private UserServiceJPA userServiceJPA;

}
