package com.example.InfBezTim10.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserData {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String telephoneNumber;
}
