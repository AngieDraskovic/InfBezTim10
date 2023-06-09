package com.example.InfBezTim10.service.accountManagement.implementation;
import com.example.InfBezTim10.controller.UserController;
import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.accountManagement.ITwillioService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TwillioService implements ITwillioService {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Override
    public void sendConfirmNumberSMS(User user, String activationId){
        Twilio.init(System.getenv("ACCOUNT_SID"),System.getenv("AUTH_TOKEN"));
        String body = "To confirm your phone number please use this code:\n\n" + activationId;
        PhoneNumber from = new PhoneNumber("+16206369991");
        PhoneNumber to = new PhoneNumber(user.getTelephoneNumber());
        Message message = Message.creator(to, from, body).create();
        logger.info("SMS has been sent to user{}!",user.getEmail());
    }

    @Override
    public void sendResetPasswordSMS(User user, String code){
        Twilio.init(System.getenv("ACCOUNT_SID"),System.getenv("AUTH_TOKEN"));
        String body = "To reset your password use this code:\n\n" + code;
        PhoneNumber from = new PhoneNumber("+16206369991");
        PhoneNumber to = new PhoneNumber(user.getTelephoneNumber());
        Message message = Message.creator(to, from, body).create();
        logger.info("SMS has been sent to user{}!",user.getEmail());
    }


    @Override
    public void sendTwoFactorAuthCodeSMS(User user, String code){
        Twilio.init(System.getenv("ACCOUNT_SID"),System.getenv("AUTH_TOKEN"));
        String body = "To login in use this code:\n\n" + code;
        PhoneNumber from = new PhoneNumber("+16206369991");
        PhoneNumber to = new PhoneNumber(user.getTelephoneNumber());
        Message message = Message.creator(to, from, body).create();
        logger.info("SMS has been sent to user{}!",user.getEmail());
    }
}
