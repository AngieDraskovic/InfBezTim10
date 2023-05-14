package com.example.InfBezTim10.model.user;

import com.example.InfBezTim10.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "two-factors")
@Getter
@AllArgsConstructor
@Setter
@RequiredArgsConstructor
public class TwoFactorAuth extends BaseEntity {

    @Indexed(unique=true)
    private String code;
    @DBRef
    private User user;
    private LocalDateTime creationDate;
    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getUser(), getCreationDate());
    }
}
