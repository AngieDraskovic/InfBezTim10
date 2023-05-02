package com.example.InfBezTim10.service.userManagement;

import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.base.IJPAService;

public interface IUserService extends IJPAService<User> {
    User findByEmail(String email);
}
