package com.example.InfBezTim10.model.user;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Document(collection = "users")
@Getter
@Setter
public class User extends BaseEntity {

    private String name;

    private String surname;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String telephoneNumber;

    private AccountStatus accountStatus;

    private List<String> previousPasswords;

    @DBRef
    private Authority authority;

    private LocalDateTime lastPasswordResetDate;


    public User() {
        super();
        this.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
    }

    public User(String name, String surname, String email, String password, String telephoneNumber) {
        super();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.telephoneNumber = telephoneNumber;
        this.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        this.setLastPasswordResetDate(LocalDateTime.now());
        this.setPreviousPasswords(new ArrayList<>());
    }
}