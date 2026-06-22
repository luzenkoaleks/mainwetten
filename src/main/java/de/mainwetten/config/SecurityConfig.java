package de.mainwetten.config;

import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import de.mainwetten.security.ratelimit.LoginRateLimitFilter;
import de.mainwetten.security.ratelimit.LoginRateLimitSuccessHandler;
import de.mainwetten.security.ratelimit.PublicFormRateLimiter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.web.authentication.RememberMeServices;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            PublicFormRateLimiter publicFormRateLimiter,
            RememberMeServices rememberMeServices,
            SessionRegistry sessionRegistry
    ) throws Exception {
        LoginRateLimitFilter loginRateLimitFilter =
                new LoginRateLimitFilter(publicFormRateLimiter);

        LoginRateLimitSuccessHandler loginSuccessHandler =
                new LoginRateLimitSuccessHandler(
                        publicFormRateLimiter
                );
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/register/**",
                                "/verify-email",
                                "/forgot-password",
                                "/reset-password",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "base-uri 'self'; " +
                                                "form-action 'self'; " +
                                                "frame-ancestors 'none'; " +
                                                "object-src 'none'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' https://cdn.jsdelivr.net; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self'; " +
                                                "connect-src 'self'"
                                )
                        )
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicy.SAME_ORIGIN)
                        )
                        .permissionsPolicyHeader(permissions -> permissions
                                .policy(
                                        "camera=(), microphone=(), geolocation=(), payment=(), usb=()"
                                )
                        )
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices)
                )
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                        .expiredSessionStrategy(
                                new SimpleRedirectSessionInformationExpiredStrategy(
                                        "/login?sessionExpired=true"
                                )
                        )
                )
                .addFilterBefore(
                        loginRateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository appUserRepository) {
        return username -> {
            AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return User.withUsername(appUser.getUsername())
                    .password(appUser.getPasswordHash())
                    .disabled(!appUser.isEmailVerified())
                    .roles("USER")
                    .build();
        };
    }
}
