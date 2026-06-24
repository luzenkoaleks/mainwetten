package de.mainwetten.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

import de.mainwetten.user.LoginIdentifierService;

public final class LoginRateLimitFilter extends OncePerRequestFilter {

    private final PublicFormRateLimiter rateLimiter;

    private final LoginIdentifierService
            loginIdentifierService;

    public LoginRateLimitFilter(
            PublicFormRateLimiter rateLimiter,
            LoginIdentifierService loginIdentifierService
    ) {
        this.rateLimiter = rateLimiter;
        this.loginIdentifierService = loginIdentifierService;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !"/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String loginIdentifier = request.getParameter(
                UsernamePasswordAuthenticationFilter
                        .SPRING_SECURITY_FORM_USERNAME_KEY
        );

        String canonicalIdentifier =
                loginIdentifierService
                        .normalizeForRateLimit(
                                loginIdentifier
                        );

        String clientKey = buildClientKey(
                request.getRemoteAddr(),
                canonicalIdentifier
        );

        if (!rateLimiter.tryConsumeLoginAttempt(clientKey)) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/login?rateLimited=true"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    static String buildClientKey(
            String remoteAddress,
            String username
    ) {
        String normalizedAddress =
                remoteAddress == null || remoteAddress.isBlank()
                        ? "unknown-client"
                        : remoteAddress.trim();

        String normalizedUsername =
                username == null || username.isBlank()
                        ? "unknown-user"
                        : username.trim().toLowerCase(Locale.ROOT);

        return normalizedAddress + "|" + normalizedUsername;
    }
}
