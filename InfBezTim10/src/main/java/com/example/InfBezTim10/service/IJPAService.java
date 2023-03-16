package com.example.InfBezTim10.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface IJPAService<T> extends ICRUDService<T> {
    Iterable<T> findAll(Sort sorter);

    Page<T> findAll(Pageable page);
}

