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
    private static final String ACCESS_TOKEN = "ya29.a0AXooCgsd9KCfprYvsBolPjRO9qmWAdInzrDIE80Z-SsoA9_3mxpC45LDUW00wvok9MlQE8IhUULgJ-7HLKlXsPRpgmz-V4WQgUloO7B-SHAlNeP1eqzeAe7jMKVAs2as7G_4CU0khAnEV6Xx_YXROqdWDykH5gbqxHJvaCgYKAWASARISFQHGX2MiRZrly7KYtXWxVnJdinGsCA0171";

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