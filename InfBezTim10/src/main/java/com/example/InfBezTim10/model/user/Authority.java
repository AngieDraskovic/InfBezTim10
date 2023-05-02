package com.example.InfBezTim10.model.user;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

@Document(collection = "authorities")
@Getter
@Setter
public class Authority extends BaseEntity implements GrantedAuthority {

    @Indexed(unique = true)
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