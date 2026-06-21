package de.mainwetten.user;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PasswordResetMailServiceTest {

    @Test
    void sendsPasswordResetMailWithResetLink() {
        MailSender mailSender = mock(MailSender.class);

        PasswordResetMailService service =
                new PasswordResetMailService(
                        mailSender,
                        "http://localhost:8080/",
                        "no-reply@mainwetten.test"
                );

        AppUser user = new AppUser();
        user.setUsername("Alice");
        user.setEmail("alice@example.test");

        service.sendPasswordResetEmail(
                user,
                "test-token_123"
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(
                        SimpleMailMessage.class
                );

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(
                "no-reply@mainwetten.test",
                message.getFrom()
        );

        assertArrayEquals(
                new String[]{"alice@example.test"},
                message.getTo()
        );

        assertEquals(
                "Mainwetten-Passwort zurücksetzen",
                message.getSubject()
        );

        assertTrue(
                message.getText().contains(
                        "http://localhost:8080/reset-password?token=test-token_123"
                )
        );
    }
}
