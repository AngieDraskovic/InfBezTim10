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
public class UserDTORequest {

    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$",  message = "Field email format is not valid!")
    private String email;
    @Length(min = 3, max=20)
    private String name;
    @Length(min = 3, max=20)
    private String surname;
    @Size(min = 5, max = 30,  message = "Field telephoneNumber is not valid!")
    private String telephoneNumber;
    @Length(min = 6)
    private String password;



}
