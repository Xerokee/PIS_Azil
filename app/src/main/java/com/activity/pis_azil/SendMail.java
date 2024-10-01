package com.activity.pis_azil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.client.util.Base64;

public class SendMail {

    private static final String APPLICATION_NAME = "Your Application Name";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String ACCESS_TOKEN = "ya29.a0AcM612zy19cPuL0nbXudBh9SDw2YKktAbcW6QwAEk28BMoJ5CKWXj-WiRuLNqJ5NK4CYfIG2-3iFTHEZmSh0AvZwJmcA_Elf2umQX3WkvBKW71ZA_RwuaRdcKQQN7qScGiuATFa7EbFZpgRDgkb-4V9Pu7jVJ8s0-nFzPzI_aCgYKAXcSARISFQHGX2MiIiaj1Em517dT2CmEhle52Q0175";

    public static void sendEmail(String to, String subject, String body) throws GeneralSecurityException, IOException, MessagingException {
        Gmail service = getGmailService(ACCESS_TOKEN);
        MimeMessage email = createEmail(to, "me", subject, body);
        sendMessage(service, "me", email);
    }

    private static Gmail getGmailService(String accessToken) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private static void sendMessage(Gmail service, String userId, MimeMessage email) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);

        service.users().messages().send(userId, message).execute();
    }
}