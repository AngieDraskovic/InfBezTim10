package com.example.InfBezTim10.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.InheritanceType.JOINED;

@Entity
@Table(name = "users")
@Inheritance(strategy=JOINED)
@Getter
@Setter
public abstract class User extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "telephone_number")
    private String telephoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "authority_id")
    private Authority authority;

    protected User() {
        super();
    }

    public User(String name, String surname, String email, String password, String telephoneNumber, Authority authority) {
        this();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.telephoneNumber = telephoneNumber;
        this.authority = authority;
    }
}