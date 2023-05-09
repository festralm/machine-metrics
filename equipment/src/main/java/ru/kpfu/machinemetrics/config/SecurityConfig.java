package ru.kpfu.machinemetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/equipment/**")
                .authorizeHttpRequests()
                .requestMatchers("/equipment/**")
                .hasAuthority("SCOPE_equipment.read")
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }
}
