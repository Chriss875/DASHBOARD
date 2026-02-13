/*package org.udsm.udsm_hackathon2026.config;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwtToken;
        final String userEmail;


        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No JWT token found in request headers");
                response.sendError(UNAUTHORIZED.value(), "No authentication token provided");
                return;
            }

            jwtToken = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwtToken);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    if (jwtService.isTokenValid(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (UsernameNotFoundException e) {
                    log.warn("User not found: {}", userEmail);
                    response.sendError(UNAUTHORIZED.value(), "User not found");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error processing authentication token: {}", e.getMessage(), e);
            response.sendError(UNAUTHORIZED.value(), "Invalid authentication token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/api/v1/products/get-all-products") ||
                path.startsWith("/api/v1/displayItem/top-picks") ||
                path.startsWith("/api/v1/displayItem/newest") ||
                path.startsWith("/api/v1/products/categories") ||
                path.startsWith("/api/v1/oauth2/") ||
                path.startsWith("/oauth2/authorization/") ||
                path.startsWith("/login/oauth2/")||
                path.startsWith("/api/v1/userManagement/reset-passwordEmail") ||
                path.startsWith("/api/v1/userManagement/verify-otp") ||
                path.startsWith("/api/v1/userManagement/new-password")||
                path.startsWith("/api/v1/products/product-clickCount/")||
                path.startsWith("/api/v1/userManagement/seller-profile")||
                path.startsWith("/api/v1/email/send-signup-Otp")||
                path.startsWith("/api/v1/email/verify-signup-otp")||
                path.startsWith("/webhook");
    }
}
*/