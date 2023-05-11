package ru.kpfu.machinemetrics.config.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.kpfu.machinemetrics.config.properties.AppApiProperties;

@Configuration
@EnableWebFluxSecurity
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
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange()

                .pathMatchers(HttpMethod.GET, appApiProperties.getV1() + "/equipment/**")
                .hasAnyAuthority(Roles.USER.value, Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.POST,  appApiProperties.getV1() + "/equipment/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.PUT,  appApiProperties.getV1() + "/equipment/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.DELETE,  appApiProperties.getV1() + "/equipment/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.GET,  appApiProperties.getV1() + "/cron/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.POST,  appApiProperties.getV1() + "/cron/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.DELETE,  appApiProperties.getV1() + "/cron/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.PUT,  appApiProperties.getV1() + "/cron/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.GET,  appApiProperties.getV1() + "/data-service/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.POST,  appApiProperties.getV1() + "/data-service/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.DELETE,  appApiProperties.getV1() + "/data-service/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.GET,  appApiProperties.getV1() + "/equipment-info/**")
                .hasAnyAuthority(Roles.USER.value, Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.POST,  appApiProperties.getV1() + "/equipment-info/**")
                .hasAnyAuthority(Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.DELETE,  appApiProperties.getV1() + "/equipment-info/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.GET,  appApiProperties.getV1() + "/equipment-data/**")
                .hasAnyAuthority(Roles.USER.value, Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(HttpMethod.GET, appApiProperties.getV1() + "/user/current")
                .hasAnyAuthority(Roles.USER.value, Roles.ADMIN.value, Roles.MODERATOR.value)

                .pathMatchers(
                        HttpMethod.GET,
                        appApiProperties.getV1() + "/user/{id}",
                        appApiProperties.getV1() + "/user"
                )
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.POST,  appApiProperties.getV1() + "/user/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.DELETE,  appApiProperties.getV1() + "/user/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .pathMatchers(HttpMethod.GET,  appApiProperties.getV1() + "/role/**")
                .hasAnyAuthority(Roles.ADMIN.value)

                .anyExchange()
                .authenticated()
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
