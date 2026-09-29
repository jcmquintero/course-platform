package com.courses.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.http.HttpMethod;
import jakarta.servlet.DispatcherType;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categories/**",
                                "/api/instructors/**",
                                "/api/courses")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/courses/*/publish",
                                "/api/courses/*/archive")
                        .hasAnyRole("ADMIN", "INSTRUCTOR")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/enrollments")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/enrollments/*/progress")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/instructors/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/courses/**")
                        .hasAnyRole("ADMIN", "INSTRUCTOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categories/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/enrollments/*/cancel")
                        .hasAnyRole("ADMIN", "STUDENT")

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .authenticated()

                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        SecretKey secretKey = new SecretKeySpec(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);

        return authenticationConverter;
    }

    @Bean
    JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
        SecretKey secretKey = new SecretKeySpec(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey));
    }
}