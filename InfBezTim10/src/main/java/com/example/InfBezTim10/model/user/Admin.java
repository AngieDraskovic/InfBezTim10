package com.example.InfBezTim10.model.user;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@TypeAlias("admin")
@Document(collection = "users")
public class Admin {
}
