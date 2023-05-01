package com.example.InfBezTim10.dto;

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
public class UserRegistrationDTO {
    @Length(min = 3, max = 20, message = "Field name is not valid!")
    private String name;

    @Length(min = 3, max = 20, message = "Field surname is not valid!")
    private String surname;

    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Field email is not valid!")
    private String email;

    @Size(min = 5, max = 30, message = "Field telephoneNumber is not valid!")
    private String telephoneNumber;

    @Length(min = 6,  message = "Field password is not valid. Minimum length are 6 characters!")
    private String password;
}
