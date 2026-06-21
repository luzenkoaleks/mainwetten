package de.mainwetten.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetMailService {

    private final MailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public PasswordResetMailService(
            MailSender mailSender,
            @Value("${app.base-url}") String baseUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.baseUrl = removeTrailingSlash(baseUrl);
        this.fromAddress = fromAddress;
    }

    public void sendPasswordResetEmail(
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
                    "Der Passwort-Reset-Token darf nicht leer sein."
            );
        }

        String resetUrl = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/reset-password")
                .queryParam("token", "{token}")
                .encode()
                .buildAndExpand(rawToken)
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Mainwetten-Passwort zurücksetzen");
        message.setText("""
                Hallo %s,

                für dein Mainwetten-Konto wurde das Zurücksetzen des Passworts angefordert.

                Öffne dafür diesen Link:

                %s

                Der Link ist eine Stunde gültig.

                Falls du diese Anfrage nicht gestellt hast, kannst du diese E-Mail ignorieren. Dein bisheriges Passwort bleibt unverändert.

                Viele Grüße
                dein Mainwetten-Team
                """.formatted(
                user.getUsername(),
                resetUrl
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
