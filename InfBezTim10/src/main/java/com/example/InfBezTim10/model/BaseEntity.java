package com.example.InfBezTim10.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
@Where(clause = "active = true")
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    public BaseEntity() {
        this.setActive(true);
    }

}
