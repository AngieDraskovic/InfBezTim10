package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDetailsDTO {
    private String name;

    private String surname;

    private String email;

    private String telephoneNumber;

    public UserDetailsDTO(User user){
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.telephoneNumber = user.getTelephoneNumber();
    }
}
