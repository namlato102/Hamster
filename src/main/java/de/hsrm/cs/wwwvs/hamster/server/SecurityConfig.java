package de.hsrm.cs.wwwvs.hamster.server;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * JWT
     * http://localhost:4200
     * Access token: F12 (Dev tools from browser) -> Application -> Session Storage
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( (requests) -> requests
                        .requestMatchers("/hamster/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer()
                .jwt()

        ;

        return http.build();

    }

    // TODO: JWT Decoder WIP
    @Bean
    public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties prop) {
        return NimbusJwtDecoder.withJwkSetUri(prop.getJwt().getJwkSetUri())
                .build(); // Issuer: Von 3rd party (Hochschule)
    }
}
