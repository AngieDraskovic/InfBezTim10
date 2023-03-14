package com.example.InfBezTim10.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "authority")
@Getter
@Setter
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "authority_name", nullable = false, unique = true)
    private String authorityName;

    public Authority() {
        super();
    }

    public Authority(String authorityName) {
        this.setAuthorityName(authorityName);
    }

    @Override
    public String getAuthority() {
        return this.authorityName;
    }
}