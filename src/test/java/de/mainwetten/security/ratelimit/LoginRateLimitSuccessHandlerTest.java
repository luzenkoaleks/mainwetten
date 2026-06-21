package de.mainwetten.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimitSuccessHandlerTest {

    @Test
    void successfulLoginClearsAttemptsForClientAndUsername()
            throws Exception {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        String clientKey =
                LoginRateLimitFilter.buildClientKey(
                        "192.0.2.10",
                        "Alice"
                );

        for (int attempt = 0; attempt < 10; attempt++) {
            assertTrue(
                    limiter.tryConsumeLoginAttempt(clientKey)
            );
        }

        assertFalse(
                limiter.tryConsumeLoginAttempt(clientKey)
        );

        LoginRateLimitSuccessHandler handler =
                new LoginRateLimitSuccessHandler(limiter);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("192.0.2.10");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "Alice",
                        "password",
                        "ROLE_USER"
                );

        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        assertEquals(
                "/dashboard",
                response.getRedirectedUrl()
        );

        assertTrue(
                limiter.tryConsumeLoginAttempt(clientKey)
        );
    }
}
