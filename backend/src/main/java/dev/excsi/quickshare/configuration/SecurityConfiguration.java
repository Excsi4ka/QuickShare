package dev.excsi.quickshare.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.excsi.quickshare.security.AuthSuccessHandler;
import dev.excsi.quickshare.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    @Bean
    public SecurityFilterChain buildSecurity(HttpSecurity http, AuthSuccessHandler successHandler) {

        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/oauth/**",
                            "/api/public/**",
                            "/",
                            "/register",
                            "/login",
                            "/download/**",
                            "/index.html",
                            "/assets/**",
                            "/error"
                    ).permitAll()
                    .anyRequest().authenticated();
                })
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

                //csrf needed only for this specific endpoint
                .csrf(csrf -> {
                    RequestMatcher csrfMatcher = request ->
                            HttpMethod.POST.matches(request.getMethod()) && request.getServletPath().equals("/api/auth/refresh");

                    csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
                    csrf.requireCsrfProtectionMatcher(csrfMatcher);
                })

                .oauth2ResourceServer(oAuthServer -> {
                    oAuthServer.jwt(Customizer.withDefaults());
                })
                .oauth2Login(oAuthLogin -> {
                    oAuthLogin.authorizationEndpoint(endpoint -> endpoint.baseUri("/api/auth/oauth2"));
                    oAuthLogin.successHandler(successHandler);
                })
                .build();
    }

    private SecretKeySpec getSecretKeySpec() {
        return new SecretKeySpec(jwtSecretKey.getBytes(), "HmacSHA256");
    }

    // AuthenticationManager might not always be injectable, supposedly, so just in case
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKeySpec()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> jwks = new ImmutableSecret<>(getSecretKeySpec());
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return email -> userService.getUserByEmail(email);
    }
}
