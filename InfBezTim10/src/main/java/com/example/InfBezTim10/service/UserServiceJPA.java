package com.example.InfBezTim10.service;

import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.repository.UserRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserServiceJPA{

    @Autowired
    private UserRepositoryJPA userRepositoryJPA;

    public User findOne(Integer id) {
        return userRepositoryJPA.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepositoryJPA.findAll();
    }

    public Page<User> findAll(Pageable page) {
        return userRepositoryJPA.findAll(page);
    }

    public User save(User passenger) {
        return userRepositoryJPA.save(passenger);
    }

    public void remove(Integer id) {
        userRepositoryJPA.deleteById(id);
    }

}
