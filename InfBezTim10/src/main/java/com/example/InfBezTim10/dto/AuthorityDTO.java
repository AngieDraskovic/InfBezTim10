package com.example.InfBezTim10.dto;

import com.example.InfBezTim10.model.Authority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthorityDTO {

    private String authorityName;

    public AuthorityDTO(Authority authority){
        this.authorityName = authority.getAuthorityName();
    }
}