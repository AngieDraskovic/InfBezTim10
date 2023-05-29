package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserData;

public interface IOAuthService {
    User processOAuthUser(String oauthId, UserData userData);
}
