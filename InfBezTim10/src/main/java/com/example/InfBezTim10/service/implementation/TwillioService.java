package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.service.ITwillioService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class TwillioService implements ITwillioService {
   

    @Override
    public void sendConfirmNumberSMS(User user, String activationId){
        Twilio.init(System.getenv("ACCOUNT_SID"),System.getenv("AUTH_TOKEN"));

        String body = "To confirm your phone number please use this code:\n\n" + activationId;
        PhoneNumber from = new PhoneNumber("+16206369991");
        PhoneNumber to = new PhoneNumber(user.getTelephoneNumber());

        Message message = Message.creator(to, from, body).create();
        System.out.println(message.getSid());
    }

    @Override
    public void sendResetPasswordSMS(User user, String code){
        Twilio.init(System.getenv("ACCOUNT_SID"),System.getenv("AUTH_TOKEN"));
        System.out.println(System.getenv("ACCOUNT_SID") + "     " + System.getenv("AUTH_TOKEN"));
        String body = "To reset your password use this code:\n\n" + code;
        PhoneNumber from = new PhoneNumber("+16206369991");
        PhoneNumber to = new PhoneNumber(user.getTelephoneNumber());

        Message message = Message.creator(to, from, body).create();
        System.out.println(message.getSid());
    }
}
