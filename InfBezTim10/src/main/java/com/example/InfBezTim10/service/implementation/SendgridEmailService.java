package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.model.User;
import com.example.InfBezTim10.service.ISendgridEmailService;
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

    private static final String SENDGRID_API_KEY = "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";

    @Override
    public void sendConfirmEmailMessage(User toUser, String code) throws IOException {
        Email from = new Email("tim961495@gmail.com");
        String subject = "Confirm your email";
        Email to = new Email(toUser.getEmail());
        Content content = new Content("text/plain", "Dear " + toUser.getName() + ", \n\nTo finish your registration use this activation code: \n"
                + code +  "\n\n" + "If you did not perform this registration please contact our support: \n" +
                "support@tim10.com\n\n Best regards,\nTim10 team!");
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() == 202) {
                System.out.println("Email sent successfully!");
            } else {
                System.out.println("Failed to send email: " + response.getBody());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

}