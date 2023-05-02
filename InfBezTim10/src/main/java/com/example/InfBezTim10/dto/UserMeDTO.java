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
public class UserMeDTO {
    private String name;

    private String surname;

    private String email;

    private String telephoneNumber;

    private AuthorityDTO authorityDTO;

    public UserMeDTO(User user){
        this.name = user.getName();
        this.surname = user.getSurname();
        this.email = user.getEmail();
        this.telephoneNumber = user.getTelephoneNumber();
        this.authorityDTO = new AuthorityDTO(user.getAuthority());
    }
}
