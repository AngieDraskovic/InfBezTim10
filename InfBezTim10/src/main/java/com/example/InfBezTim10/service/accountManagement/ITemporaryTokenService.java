package com.example.InfBezTim10.service.accountManagement;

import com.example.InfBezTim10.model.auth.TemporaryToken;
import com.example.InfBezTim10.service.base.IJPAService;

public interface ITemporaryTokenService extends IJPAService<TemporaryToken> {
    void storeTemporaryToken(String email, String token);

    boolean isValidTemporaryToken(String email, String token);

    void removeTemporaryToken(String email);
}
