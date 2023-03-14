package com.example.InfBezTim10.service;

import java.util.List;

public interface ICRUDService<T> {
    List<T> findAll();
    T findById(Long id);
    T save(T entity);
    T update(T entity);
    void delete(Long id);
}
