package com.dailycodework.buynowdotcom.security.config;

import com.dailycodework.buynowdotcom.response.ApiResponse;
import com.dailycodework.buynowdotcom.security.jwt.AuthTokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import com.dailycodework.buynowdotcom.security.jwt.JwtEntryPoint;
import com.dailycodework.buynowdotcom.security.user.ShopUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ShopConfig {

    private static final String API = "/api/v1";

    /** Catalog resources the storefront must be able to read without logging in. */
    private static final String[] PUBLIC_READ = {
            API + "/products/**", API + "/categories/**", API + "/images/**"
    };

    /** Per-user resources. Authentication is enforced here; ownership is enforced in the service layer. */
    private static final String[] OWNER_SCOPED = {
            API + "/carts/**", API + "/cartItems/**", API + "/orders/**", API + "/users/**"
    };

    private final ShopUserDetailsService userDetailsService;
    private final JwtEntryPoint authEntryPoint;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS must be enabled inside the security chain, not only at the MVC layer:
                // otherwise the preflight OPTIONS request is rejected by the authorization
                // rules below before it ever reaches a controller.
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        // Authenticated but lacking the required authority is a 403, not a 401 —
                        // without this the request falls through to the entry point and the
                        // client cannot tell "log in" apart from "you may not do this".
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --- public: authentication and registration ---
                        .requestMatchers(API + "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, API + "/users/add").permitAll()
                        // --- public: read-only storefront ---
                        .requestMatchers(HttpMethod.GET, PUBLIC_READ).permitAll()
                        // --- admin only: every catalog mutation ---
                        .requestMatchers(HttpMethod.POST, PUBLIC_READ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, PUBLIC_READ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, PUBLIC_READ).hasRole("ADMIN")
                        // --- authenticated: per-user resources ---
                        .requestMatchers(OWNER_SCOPED).authenticated()
                        // --- default deny: a new endpoint is protected until someone opts it out ---
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(),
                    new ApiResponse("Access denied: insufficient privileges", null));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:5175"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
