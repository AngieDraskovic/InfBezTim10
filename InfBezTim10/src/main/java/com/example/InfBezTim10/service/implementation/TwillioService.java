package com.example.InfBezTim10.service.implementation;
import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.service.ITwillioService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class TwillioService implements ITwillioService {
    public static final String ACCOUNT_SID = "AC3267838dcbbc53aa3a8eb574e0e7d5f3";
    public static final String AUTH_TOKEN = "428034449208d4fb7b662c7b3dce657e";

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
