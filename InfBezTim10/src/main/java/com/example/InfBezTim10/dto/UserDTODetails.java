package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDTODetails {
    private String email;
    private String name;
    private String surname;
    private String telephoneNumber;
    private String password;

    public UserDTODetails(User user){
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.telephoneNumber = user.getTelephoneNumber();
        this.password = user.getPassword();
    }
}
