package com.example.InfBezTim10.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Id
    private String id;

    private Boolean active;

    public BaseEntity() {
        this.setActive(true);
    }

}

