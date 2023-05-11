package ru.kpfu.machinemetrics.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.kpfu.machinemetrics.properties.AppApiProperties;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppApiProperties appApiProperties;
    private final JwtAuthConverter jwtAuthConverter;

    @Getter
    @AllArgsConstructor
    private enum Roles {
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
        MODERATOR("ROLE_MODERATOR");

        private String value;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()

                .requestMatchers(HttpMethod.GET, appApiProperties.getV1() + "/user/current")
                .hasAnyAuthority(Roles.USER.value, Roles.ADMIN.value, Roles.MODERATOR.value)

                .requestMatchers(
                        HttpMethod.GET,
                        appApiProperties.getV1() + "/user/{id}",
                        appApiProperties.getV1() + "/user"
                )
                .hasAnyAuthority(Roles.ADMIN.value)

                .requestMatchers(HttpMethod.POST, appApiProperties.getV1() + "/user/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .requestMatchers(HttpMethod.DELETE, appApiProperties.getV1() + "/user/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .requestMatchers(HttpMethod.GET, appApiProperties.getV1() + "/role/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .anyRequest().authenticated()
                .and()

                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter)
                .and().and()

                .cors()
                .and()

                .csrf()
                .disable();

        return http.build();
    }
}
