package de.mainwetten.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class RememberMeConfig {

    private static final int TOKEN_VALIDITY_SECONDS =
            Math.toIntExact(Duration.ofDays(30).toSeconds());

    @Bean
    public PersistentTokenRepository persistentTokenRepository(
            DataSource dataSource
    ) {
        JdbcTokenRepositoryImpl repository =
                new JdbcTokenRepositoryImpl();

        repository.setDataSource(dataSource);

        return repository;
    }

    @Bean
    public RememberMeServices rememberMeServices(
            PersistentTokenRepository tokenRepository,
            UserDetailsService userDetailsService,
            @Value("${app.security.remember-me-key}")
            String rememberMeKey,
            @Value("${app.security.remember-me-secure-cookie}")
            boolean secureCookie
    ) {
        PersistentTokenBasedRememberMeServices services =
                new PersistentTokenBasedRememberMeServices(
                        rememberMeKey,
                        userDetailsService,
                        tokenRepository
                );

        services.setParameter("remember-me");
        services.setCookieName("mainwetten-remember-me");
        services.setTokenValiditySeconds(
                TOKEN_VALIDITY_SECONDS
        );
        services.setUseSecureCookie(secureCookie);

        return services;
    }
}
