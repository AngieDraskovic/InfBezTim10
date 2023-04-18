package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.model.UserActivation;
import com.example.InfBezTim10.repository.IUserActivationRepository;
import com.example.InfBezTim10.service.IUserActivationService;
import com.example.InfBezTim10.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public class UserActivationService extends MongoService<UserActivation> implements IUserActivationService {

    private final IUserActivationRepository userActivationRepository;
    private final IUserService userService;

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
    public void activate(String activationId) throws  NotFoundException {
        UserActivation userActivation = userActivationRepository.findByActivationId(activationId);
        if(userActivation == null){
            throw new NotFoundException("Not existing activation!" );
        }
        User user = userActivation.getUser();
        user.setActive(Boolean.TRUE);
        userService.save(user);
        userActivationRepository.delete(userActivation);

    }

    @Override
    public void deleteIfAlreadyExists(User user){
        if (userActivationRepository.existsByUser(user)) {
            userActivationRepository.deleteByUser(user);
        }
    }

    @Override
    public UserActivation save(UserActivation activation){
        return userActivationRepository.save(activation);
    }

}
