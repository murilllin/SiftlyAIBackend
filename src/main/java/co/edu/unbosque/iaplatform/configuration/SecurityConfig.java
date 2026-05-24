package co.edu.unbosque.iaplatform.configuration;

import co.edu.unbosque.iaplatform.security.JwtAuthFilter;
import co.edu.unbosque.iaplatform.service.OAuth2UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración principal de seguridad de Spring Security.
 * Define políticas de autorización, CORS, OAuth2 y filtro JWT.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private OAuth2UsuarioService oAuth2UsuarioService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; connect-src 'self'"))
                .frameOptions(fo -> fo.deny())
                .xssProtection(xss -> xss.disable()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST,
                    "/auth/registro", "/auth/login", "/auth/verificar-codigo",
                    "/auth/refresh", "/auth/reenviar-codigo",
                    "/api/auth/registro", "/api/auth/login", "/api/auth/verificar-codigo",
                    "/api/auth/refresh", "/api/auth/reenviar-codigo").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/auth/oauth2/**", "/auth/public/**",
                    "/api/auth/oauth2/**", "/api/auth/public/**").permitAll()
                .requestMatchers(
                    "/login/oauth2/code/**", "/oauth2/**",
                    "/api/login/oauth2/code/**", "/api/oauth2/**").permitAll()
                .requestMatchers(
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                .requestMatchers("/media/**", "/api/media/**").permitAll()
                .requestMatchers("/proxy/**", "/api/proxy/**").permitAll()
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(ui -> ui.userService(oAuth2UsuarioService))
                .successHandler(oAuth2SuccessHandler())
                .failureHandler((req, res, ex) -> {
                    log.error("=== OAuth2 FALLO ===");
                    res.sendRedirect(frontendUrl + "/login?error=oauth2");
                }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:4201", frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "X-Refresh-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler();
    }
}
