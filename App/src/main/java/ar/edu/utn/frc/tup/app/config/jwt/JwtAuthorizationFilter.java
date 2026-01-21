package ar.edu.utn.frc.tup.app.config.jwt;

import ar.edu.utn.frc.tup.app.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.http.HttpHeaders;


@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Log for debugging
        logger.info("Checking filter for path: " + path + " method: " + method);
        
        boolean skip = path.startsWith("/api/v1/auth/") ||
               path.startsWith("/api/v1/registro/") ||
               path.startsWith("/api/v1/password/") ||
               path.startsWith("/api/v1/domicilios/") ||
               path.startsWith("/api/v1/usuario/") ||
               path.startsWith("/api/v1/resenias/") ||
               path.startsWith("/api/v1/pagos/") ||
               path.startsWith("/api/v1/oficios/") ||
               path.startsWith("/api/v1/perfil/profesional/oficio/") ||
               path.startsWith("/api/v1/perfil/profesionales/") ||
               path.startsWith("/api/v1/solicitudes/profesionales/") ||
               (path.startsWith("/api/v1/galeria/profesional/") && method.equals("GET")) ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/swagger-ui/") ||
               path.equals("/swagger-ui.html") ||
               method.equals("OPTIONS"); // Always skip OPTIONS requests
        
        if (skip) {
            logger.info("Skipping JWT filter for: " + method + " " + path);
        }
        
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String token = getTokenFromRequest(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            final String username = jwtService.getUsernameFromToken(token);

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if(jwtService.isTokenValid(token, userDetails)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expirado\", \"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // Log the exception but continue with the filter chain
            // This allows public endpoints to work without authentication
            logger.warn("Error processing JWT token: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        return null;
    }
}
