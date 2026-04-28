package com.officedubac.project.config;

import com.officedubac.project.services.ParametrageService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ParametrageService parametrageService;
    private static final String API_KEY_HEADER = "API-Key";
    private static final String API_SECRET_HEADER = "API-Secret";
    private final AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/api/v1/office-du-bac/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        String apiSecret = request.getHeader(API_SECRET_HEADER);

        if (apiKey == null || apiSecret == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing API Key or Secret");
            return;
        }

        if (parametrageService.isValid(apiKey, apiSecret)) {

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            apiKey, apiSecret,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        else
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key or Secret");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
