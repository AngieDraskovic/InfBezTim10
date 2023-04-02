package com.example.InfBezTim10.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
public class User extends BaseEntity {

    private String name;

    private String surname;

    private String email;

    private String password;

    private String telephoneNumber;

    @DBRef
    private Authority authority;

    public User() {
        super();
    }

    public User(String name, String surname, String email, String password, String telephoneNumber) {
        super();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.telephoneNumber = telephoneNumber;
    }
}