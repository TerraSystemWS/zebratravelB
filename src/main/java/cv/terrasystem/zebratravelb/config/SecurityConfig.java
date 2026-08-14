package cv.terrasystem.zebratravelb.config;

import cv.terrasystem.zebratravelb.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private static final String[] PUBLIC_GET_PATHS = {
            "/api/tours/**",
            "/api/excursions/**",
            "/api/products/**",
            "/api/posts/**",
            "/api/faqs/**",
            "/api/gallery/**",
            "/api/sponsors/**",
            "/api/team-members/**",
            "/api/testimonials/**",
            "/api/travel-packages/**",
            "/api/content/**",
            "/api/settings/maintenance",
            "/api/hotels/**",
            "/api/room-types/*",
            "/api/rooms/*",
            "/api/rooms/*/reviews",
            "/api/excursions/*/reviews",
            "/api/hotel-amenities",
            "/api/campaigns/active"
    };

    // Endpoints POST públicos (sem autenticação) — formulários do site que não exigem login.
    private static final String[] PUBLIC_POST_PATHS = {
            "/api/subscribers",
            "/api/job-applications",
            "/api/contact-messages",
            "/api/campaigns/*/click"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/payments/vinti4/callback").permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                // Sem isto, o Spring Security usa o Http403ForbiddenEntryPoint por omissão para pedidos sem
                // autenticação válida (token em falta/inválido/expirado) - devolve 403 igual a um @PreAuthorize
                // a bloquear por falta de role, e o frontend não consegue distinguir "sessão expirada" de
                // "não tens permissão para isto". Com isto, falta de autenticação passa a ser sempre 401.
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado")))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        // O callback da SISP/Vinti4 é chamado a partir do domínio deles, não do nosso frontend -
        // não pode ficar preso à allowlist de origens do site (senão a resposta do pagamento nunca chega, 403).
        CorsConfiguration paymentCallbackConfig = new CorsConfiguration();
        paymentCallbackConfig.setAllowedOriginPatterns(List.of("*"));
        paymentCallbackConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        paymentCallbackConfig.setAllowedHeaders(List.of("*"));
        paymentCallbackConfig.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/payments/vinti4/callback", paymentCallbackConfig);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
