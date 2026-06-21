package de.mainwetten.security.ratelimit;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public final class LoginRateLimitSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    private final PublicFormRateLimiter rateLimiter;

    public LoginRateLimitSuccessHandler(
            PublicFormRateLimiter rateLimiter
    ) {
        this.rateLimiter = rateLimiter;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        String clientKey = LoginRateLimitFilter.buildClientKey(
                request.getRemoteAddr(),
                authentication.getName()
        );

        rateLimiter.clearLoginAttempts(clientKey);

        super.onAuthenticationSuccess(
                request,
                response,
                authentication
        );
    }
}
