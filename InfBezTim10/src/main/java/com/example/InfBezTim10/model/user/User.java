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

    @DBRef
    private Authority authority;

    private String oauthId;

    private List<String> previousPasswords;

    private LocalDateTime lastPasswordResetDate;


    public User() {
        super();
        this.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
    }

    public static User createNormalUser(String email, String password, String name, String surname, String telephoneNumber) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setSurname(surname);
        user.setTelephoneNumber(telephoneNumber);
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        user.setLastPasswordResetDate(LocalDateTime.now());
        user.setPreviousPasswords(new ArrayList<>());
        return user;
    }

    public static User createOAuthUser(String email, String password, String name, String surname, String oauthId) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setSurname(surname);
        user.setOauthId(oauthId);
        user.setAccountStatus(AccountStatus.AUTHENTICATED_THROUGH_OAUTH);
        user.setLastPasswordResetDate(LocalDateTime.now());
        user.setPreviousPasswords(new ArrayList<>());
        return user;
    }
}