package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDetailsDTO {
    private String email;
    private String name;
    private String surname;
    private String telephoneNumber;
    private String password;

    public UserDetailsDTO(User user){
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.telephoneNumber = user.getTelephoneNumber();
        this.password = user.getPassword();
    }
}
