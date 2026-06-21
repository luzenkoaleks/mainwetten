package de.mainwetten.user;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmailVerificationMailServiceTest {

    @Test
    void sendVerificationEmail_sendsMailWithVerificationLink() {
        MailSender mailSender = mock(MailSender.class);

        EmailVerificationMailService service =
                new EmailVerificationMailService(
                        mailSender,
                        "http://localhost:8080/",
                        "no-reply@mainwetten.test"
                );

        AppUser user = new AppUser();
        user.setUsername("Alice");
        user.setEmail("alice@example.test");

        service.sendVerificationEmail(
                user,
                "test-token_123"
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals(
                "no-reply@mainwetten.test",
                message.getFrom()
        );

        assertArrayEquals(
                new String[]{"alice@example.test"},
                message.getTo()
        );

        assertEquals(
                "E-Mail-Adresse für Mainwetten bestätigen",
                message.getSubject()
        );

        assertTrue(message.getText().contains("Hallo Alice"));
        assertTrue(
                message.getText().contains(
                        "http://localhost:8080/verify-email?token=test-token_123"
                )
        );
    }

    @Test
    void sendVerificationEmail_rejectsBlankToken() {
        MailSender mailSender = mock(MailSender.class);

        EmailVerificationMailService service =
                new EmailVerificationMailService(
                        mailSender,
                        "http://localhost:8080",
                        "no-reply@mainwetten.test"
                );

        AppUser user = new AppUser();
        user.setUsername("Alice");
        user.setEmail("alice@example.test");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendVerificationEmail(user, " ")
        );

        verify(mailSender, never())
                .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationEmail_rejectsMissingEmailAddress() {
        MailSender mailSender = mock(MailSender.class);

        EmailVerificationMailService service =
                new EmailVerificationMailService(
                        mailSender,
                        "http://localhost:8080",
                        "no-reply@mainwetten.test"
                );

        AppUser user = new AppUser();
        user.setUsername("Alice");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.sendVerificationEmail(
                        user,
                        "test-token"
                )
        );

        verify(mailSender, never())
                .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
