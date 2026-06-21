package de.mainwetten.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EmailVerificationMailService {

    private final MailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public EmailVerificationMailService(
            MailSender mailSender,
            @Value("${app.base-url}") String baseUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(
            AppUser user,
            String rawToken
    ) {
        if (user == null
                || user.getEmail() == null
                || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Für den Benutzer ist keine gültige E-Mail-Adresse vorhanden."
            );
        }

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Verifikationstoken darf nicht leer sein."
            );
        }

        String verificationUrl = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/verify-email")
                .queryParam("token", "{token}")
                .encode()
                .buildAndExpand(rawToken)
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("E-Mail-Adresse für Mainwetten bestätigen");
        message.setText("""
                Hallo %s,

                bitte bestätige deine E-Mail-Adresse, um dein Mainwetten-Konto zu aktivieren.

                Öffne dafür diesen Link:

                %s

                Falls du dich nicht bei Mainwetten registriert hast, kannst du diese E-Mail ignorieren.

                Viele Grüße
                dein Mainwetten-Team
                """.formatted(
                user.getUsername(),
                verificationUrl
        ));

        mailSender.send(message);
    }

    private String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Die Basis-URL für Mainwetten darf nicht leer sein."
            );
        }

        String normalizedValue = value.trim();

        while (normalizedValue.endsWith("/")) {
            normalizedValue = normalizedValue.substring(
                    0,
                    normalizedValue.length() - 1
            );
        }

        return normalizedValue;
    }
}
