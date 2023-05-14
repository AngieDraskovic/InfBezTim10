package com.example.InfBezTim10.service.accountManagement.implementation;

import com.example.InfBezTim10.model.user.User;
import com.example.InfBezTim10.service.accountManagement.ISendgridEmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendgridEmailService implements ISendgridEmailService {

    public void sendConfirmEmailMessage(User toUser, String code) {
        String fromEmail = "tim961495@gmail.com";
        String subject = "Confirm your email";
        String toEmail = toUser.getEmail();
        String messageBody = createEmailMessageBody(toUser.getName(), code);

        try {
            sendEmail(fromEmail, subject, toEmail, messageBody);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String createEmailMessageBody(String userName, String code) {
        return "Dear " + userName + ",\n\nTo finish your registration, use this activation code:\n" +
                code + "\n\nIf you did not perform this registration, please contact our support:\n" +
                "support@tim10.com\n\nBest regards,\nTim10 team!";
    }

    public void sendNewPasswordMail(User toUser, String code) {
        String fromEmail = "tim961495@gmail.com";
        String subject = "New Password Request";
        String toEmail = toUser.getEmail();
        String messageBody = createNewPasswordEmailMessageBody(toUser.getName(), code);

        try {
            sendEmail(fromEmail, subject, toEmail, messageBody);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendTwoFactorAuthCodeMail(User toUser, String code){
        String fromEmail = "tim961495@gmail.com";
        String subject = "Two Factor Authentication";
        String toEmail = toUser.getEmail();
        String messageBody =createAuthenticationEmailMessageBody(toUser.getName(), code);
        try {
            sendEmail(fromEmail, subject, toEmail, messageBody);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String createNewPasswordEmailMessageBody(String userName, String code) {
        return "Dear " + userName + ",\n\nTo reset your password, use this code:\n" +
                code + "\n\nBest regards,\nTim10 team!";
    }

    private String createAuthenticationEmailMessageBody(String userName, String code) {
        return "Dear " + userName + ",\n\nTo login, use this code:\n" +
                code + "\n\nBest regards,\nTim10 team!";
    }
    private void sendEmail(String fromEmail, String subject, String toEmail, String messageBody) throws IOException {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", messageBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        Request request = createSendGridRequest(mail);

        Response response = sg.api(request);
        handleResponse(response);
    }

    private Request createSendGridRequest(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return request;
    }

    private void handleResponse(Response response) {
        if (response.getStatusCode() == 202) {
            System.out.println("Email sent successfully!");
        } else {
            System.out.println("Failed to send email: " + response.getBody());
        }
    }
}
