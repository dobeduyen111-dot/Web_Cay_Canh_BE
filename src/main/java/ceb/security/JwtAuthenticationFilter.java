package ceb.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import ceb.exception.DomainException;
import ceb.exception.ErrorCode;
import ceb.exception.InvalidJwtTokenException;
import ceb.exception.TokenUserMismatchException;
import ceb.exception.UserDisabledException;
import ceb.service.implement.DatabaseUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String LOGOUT_PATH = "/api/auth/logout";

    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final DatabaseUserDetailsService userDetailsService;
    private final SecurityErrorWriter securityErrorWriter;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            JwtCookieService jwtCookieService,
            DatabaseUserDetailsService userDetailsService,
            SecurityErrorWriter securityErrorWriter,
            TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
        this.userDetailsService = userDetailsService;
        this.securityErrorWriter = securityErrorWriter;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod()) && LOGOUT_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = jwtCookieService.resolveToken(request).orElse(null);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                throw new InvalidJwtTokenException();
            }

            JwtService.JwtUserClaims claims = jwtService.verifyAndExtract(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(claims.username());

            if (!(userDetails instanceof CustomUserDetails customUserDetails)
                    || customUserDetails.getUserId() != claims.userId()
                    || !customUserDetails.getUsername().equals(claims.username())) {
                throw new TokenUserMismatchException();
            }
            if (!customUserDetails.isEnabled()) {
                throw new UserDisabledException();
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.TOKEN_USER_MISMATCH,
                    ErrorCode.TOKEN_USER_MISMATCH.getMessage());
        } catch (DomainException ex) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(response, HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage());
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.INVALID_JWT_TOKEN,
                    ErrorCode.INVALID_JWT_TOKEN.getMessage());
        }
    }
}
