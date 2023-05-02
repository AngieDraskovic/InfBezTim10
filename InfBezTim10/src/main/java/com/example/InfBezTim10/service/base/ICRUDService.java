package com.example.InfBezTim10.service.base;

import java.util.List;

public interface ICRUDService<T> {
    List<T> findAll();
    T findById(String id);
    T save(T entity);
    T update(T entity);
    void delete(String id);
}
