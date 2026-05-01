package com.officedubac.project.config;

import com.officedubac.project.models.Role;
import com.officedubac.project.services.UserService;
import com.officedubac.project.models.Profil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;
    private final ApiKeyAuthFilter apiKeyAuthFilter;

    //🔹 Configuration pour l'authentification via API Key (`/v1/api/achatOnline/**`).

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception
    {
         http.securityMatcher("/api/v1/office-du-bac/**")
         .cors(cors -> cors.configurationSource(request -> {
             CorsConfiguration cors1 = new CorsConfiguration();
             cors1.setAllowedOriginPatterns(List.of("*"));
             cors1.setAllowedMethods(List.of("GET", "OPTIONS"));
             cors1.setAllowedHeaders(List.of("Authorization", "Content-Type", "API-Key", "API-Secret"));
             cors1.setExposedHeaders(List.of("Authorization"));
             cors1.setAllowCredentials(true);
         return cors1;
     }))
         .csrf(AbstractHttpConfigurer::disable)
         .authorizeHttpRequests(auth -> auth
                 .anyRequest()
                 .permitAll()
     )
     .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

     return http.build();
     }

    // Configuration pour les requêtes nécessitant un JWT (ex : `/api/v1/auth/**`).

    @Bean
    @Order(2)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self' http://localhost:3000")))
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cors1 = new CorsConfiguration();
                    cors1.setAllowedOriginPatterns(List.of("*"));
                    cors1.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    cors1.setAllowedHeaders(List.of(
                            "Authorization",
                            "Content-Type",
                            "Cache-Control",
                            "Pragma",
                            "Expires"
                    ));
                    cors1.setExposedHeaders(List.of("Authorization"));
                    cors1.setAllowCredentials(true);
                    return cors1;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/authentification/**").permitAll()
                        .requestMatchers("/api/v1/callback").permitAll()
                        //.requestMatchers("/api/v1/enrollment-cgs/**").permitAll()
                        .requestMatchers("/api/v1/import-data/**").permitAll()
                        .requestMatchers("/api/v1/pdf/**").hasAnyAuthority(Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.SCOLARITE.name(), Role.RECEPTIONNISTE.name(), Role.ADMIN.name())
                        .requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs","/webjars/**", "/v3/api-docs/swagger-config").permitAll()
                        .requestMatchers("/api/v1/parametrage/**").hasAnyAuthority(Role.ADMIN.name(), Role.SCOLARITE.name())
                        .requestMatchers("/api/v1/enrollment-candidats/**").hasAnyAuthority(Role.INSPECTEUR_ACADEMIE.name(), Role.FINANCE_COMPTA.name(), Role.ADMIN.name(), Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.SCOLARITE.name(), Role.RECEPTIONNISTE.name(), Role.AUTORISATION_RECEPTION.name(), Role.VIGNETTES_COUPONS.name(), Role.DEMSG.name())
                        .requestMatchers("/api/v1/security/**").hasAnyAuthority(Role.ADMIN.name(), Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.SCOLARITE.name(), Role.RECEPTIONNISTE.name(), Role.AUTORISATION_RECEPTION.name(), Role.VIGNETTES_COUPONS.name(), Role.INSPECTEUR_ACADEMIE.name(), Role.DEMSG.name(), Role.FINANCE_COMPTA.name())
                        .requestMatchers("/api/v1/files/**").hasAnyAuthority(Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.SCOLARITE.name(), Role.ADMIN.name(), Role.AUTORISATION_RECEPTION.name(), Role.VIGNETTES_COUPONS.name(), Role.FINANCE_COMPTA.name())
                        .requestMatchers("/api/v1/validation-candidats/**").hasAnyAuthority(Role.AUTORISATION_RECEPTION.name(), Role.FINANCE_COMPTA.name(), Role.SCOLARITE.name(), Role.VIGNETTES_COUPONS.name(), Role.AGENT_DE_SAISIE.name(), Role.ADMIN.name(), Role.RECEPTIONNISTE.name(), Role.INSPECTEUR_ACADEMIE.name(), Role.DEMSG.name())
                        .requestMatchers("/api/v1/payment-FAEB3/**").hasAnyAuthority(Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.ADMIN.name(), Role.FINANCE_COMPTA.name())
                        .requestMatchers("/api/v1/notifications/**").hasAnyAuthority(Role.SCOLARITE.name(), Role.VIGNETTES_COUPONS.name())
                        .requestMatchers("/api/v1/enrollment-cgs/**").hasAnyAuthority(Role.SCOLARITE.name(), Role.ADMIN.name())
                        .requestMatchers("/api/v1/stats/**").hasAnyAuthority(Role.ADMIN.name(), Role.SCOLARITE.name(), Role.INSPECTEUR_ACADEMIE.name(), Role.DEMSG.name(), Role.FINANCE_COMPTA.name())
                        .requestMatchers("/api/v1/candidats/**").hasAnyAuthority(Role.CHEF_ETABLISSEMENT.name(), Role.AGENT_DE_SAISIE.name(), Role.ADMIN.name())
                        .requestMatchers("/api/v1/jours/**").permitAll()
                        .requestMatchers("/api/v1/heures/**").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/convocations/**").permitAll()
                        .requestMatchers("/api/v1/convocationMassive/**").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/convocations/batch/**").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/epreuves/**").hasAnyAuthority(Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //Fournisseur d'authentification depuis la base de données.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    // Gestionnaire d'authentification.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Encodeur du mot de passe.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}