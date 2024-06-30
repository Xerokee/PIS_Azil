package com.activity.pis_azil;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridAPI;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;

public class EmailService {

    private static final String SENDGRID_API_KEY = "YOUR_SENDGRID_API_KEY";

    public static void sendAdoptionEmail(String toEmail, String adopterName, String petName) throws IOException {
        Email from = new Email("matija.margeta@vuv.hr");
        String subject = "Potvrda udomljavanja životinje";
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", "Poštovani " + adopterName + ",\n\nUspješno ste udomili životinju: " + petName + ".\n\nHvala vam!");

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }
}

