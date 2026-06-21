package de.mainwetten.security.ratelimit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoginRateLimitFilterTest {

    @Test
    void allowsTenLoginAttemptsAndBlocksEleventh()
            throws Exception {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        LoginRateLimitFilter filter =
                new LoginRateLimitFilter(limiter);

        FilterChain filterChain = mock(FilterChain.class);

        for (int attempt = 0; attempt < 10; attempt++) {
            MockHttpServletRequest request =
                    createLoginRequest(
                            "192.0.2.10",
                            "Alice"
                    );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            filter.doFilter(
                    request,
                    response,
                    filterChain
            );
        }

        verify(filterChain, times(10))
                .doFilter(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

        MockHttpServletRequest blockedRequest =
                createLoginRequest(
                        "192.0.2.10",
                        "alice"
                );

        MockHttpServletResponse blockedResponse =
                new MockHttpServletResponse();

        filter.doFilter(
                blockedRequest,
                blockedResponse,
                filterChain
        );

        assertEquals(
                "/login?rateLimited=true",
                blockedResponse.getRedirectedUrl()
        );

        verify(filterChain, times(10))
                .doFilter(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void ignoresRequestsOtherThanPostLogin()
            throws Exception {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        LoginRateLimitFilter filter =
                new LoginRateLimitFilter(limiter);

        FilterChain filterChain = mock(FilterChain.class);

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/login"
                );

        request.setServletPath("/login");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest createLoginRequest(
            String remoteAddress,
            String username
    ) {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/login"
                );

        request.setServletPath("/login");
        request.setRemoteAddr(remoteAddress);
        request.addParameter("username", username);
        request.addParameter("password", "wrong-password");

        return request;
    }
}
