package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.exception.user.UserActivationNotFoundException;
import com.example.InfBezTim10.exception.user.UserNotFoundException;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.model.user.UserActivation;
import com.example.InfBezTim10.repository.IUserActivationRepository;
import com.example.InfBezTim10.service.accountManagement.IUserActivationService;
import com.example.InfBezTim10.service.userManagement.IUserService;
import com.example.InfBezTim10.service.base.implementation.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserActivationService extends MongoService<UserActivation> implements IUserActivationService {

    private final IUserActivationRepository userActivationRepository;
    private final IUserService userService;
    private final Random rand = new Random();

    @Autowired
    public UserActivationService(IUserActivationRepository userActivationRepository, IUserService userService) {
        this.userActivationRepository = userActivationRepository;
        this.userService = userService;
    }

    @Override
    protected MongoRepository<UserActivation, String> getEntityRepository() {
        return this.userActivationRepository;
    }

    @Override
    public void activate(String activationId) throws NotFoundException {
        UserActivation userActivation = userActivationRepository.findById(activationId).orElseThrow(() ->
                new UserActivationNotFoundException("User activation not found"));
        User user = userActivation.getUser();
        user.setActive(Boolean.TRUE);
        userService.save(user);
        userActivationRepository.delete(userActivation);
    }

    @Override
    public UserActivation create(User user) throws NotFoundException {
        deleteIfAlreadyExists(user);
        ZoneOffset desiredOffset = ZoneOffset.of("+04:00");
        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(desiredOffset);
        UserActivation activation = new UserActivation(String.valueOf(rand.nextInt(Integer.MAX_VALUE)), user,
                zonedDateTime.toLocalDateTime());
        return save(activation);
    }

    @Override
    public void deleteIfAlreadyExists(User user) {
        if (userActivationRepository.existsByUser(user)) {
            userActivationRepository.deleteByUser(user);
        }
    }
}
