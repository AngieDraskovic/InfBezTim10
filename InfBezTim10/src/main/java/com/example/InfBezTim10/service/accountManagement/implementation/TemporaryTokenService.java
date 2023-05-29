package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.model.auth.TemporaryToken;
import com.example.InfBezTim10.repository.ITemporaryTokenRepository;
import com.example.InfBezTim10.service.accountManagement.ITemporaryTokenService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TemporaryTokenService extends MongoService<TemporaryToken> implements ITemporaryTokenService {

    private final ITemporaryTokenRepository temporaryTokenRepository;

    @Autowired
    public TemporaryTokenService(ITemporaryTokenRepository temporaryTokenRepository) {
        this.temporaryTokenRepository = temporaryTokenRepository;
    }

    @Override
    public void storeTemporaryToken(String email, String token) {
        TemporaryToken existingToken = temporaryTokenRepository.findByEmail(email);
        if (existingToken != null) {
            temporaryTokenRepository.delete(existingToken);
        }

        TemporaryToken temporaryToken = new TemporaryToken();
        temporaryToken.setEmail(email);
        temporaryToken.setToken(token);
        temporaryToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        save(temporaryToken);
    }

    @Override
    public boolean isValidTemporaryToken(String email, String token) {
        TemporaryToken temporaryToken = temporaryTokenRepository.findByEmail(email);
        if (temporaryToken == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(temporaryToken.getExpiryDate())) {
            temporaryTokenRepository.delete(temporaryToken);
            return false;
        }

        return token.equals(temporaryToken.getToken());
    }

    @Override
    public void removeTemporaryToken(String email) {
        TemporaryToken temporaryToken = temporaryTokenRepository.findByEmail(email);
        if (temporaryToken != null) {
            temporaryTokenRepository.delete(temporaryToken);
        }
    }

    @Override
    protected MongoRepository<TemporaryToken, String> getEntityRepository() {
        return temporaryTokenRepository;
    }
}
