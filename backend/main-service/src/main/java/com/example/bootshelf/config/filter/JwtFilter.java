package com.example.bootshelf.config.filter;


import com.example.bootshelf.common.error.ErrorCode;
import com.example.bootshelf.common.error.ErrorResponse;
import com.example.bootshelf.config.utils.JwtUtils;
import com.example.bootshelf.common.error.entityexception.UserException;
import com.example.bootshelf.user.model.entity.User;
import com.example.bootshelf.user.repository.UserRepository;
import com.example.bootshelf.user.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtFilter extends OncePerRequestFilter {

    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    @Value("${jwt.secret-key}")
    private String secretKey;


    public JwtFilter(String secretKey, UserRepository userRepository, RefreshTokenService refreshTokenService){
        this.userRepository = userRepository;
        this.secretKey = secretKey;
        this.refreshTokenService =refreshTokenService;
    }



    // 필터에서 예외를 다루기 위한 처리
    private void handleJwtException(HttpServletResponse response, UserException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
        response.setContentType("application/json");

        // ErrorResponse 생성 및 반환
        ErrorCode errorCode;
        if (exception.getErrorCode() != null) {
            errorCode = exception.getErrorCode();
        } else {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), exception.getMessage());
        String jsonErrorResponse = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(jsonErrorResponse);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            String token;
            if (header != null && header.startsWith("Bearer ")) {
                token = header.split(" ")[1];
            } else {
                filterChain.doFilter(request, response);
                return;
            }

            String authority = JwtUtils.getAuthority(token, secretKey);

            if (authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_AUTHUSER") || authority.equals("ROLE_KAKAO")){
                String userEmail = JwtUtils.getUserEmail(token, secretKey);
                if (userEmail != null) {
                    Optional<User> result = userRepository.findByEmail(userEmail);

                    if (result.isPresent()) {
                        User user = result.get();

                        // 인가하는 코드
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user, null,
                                user.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }catch (ExpiredJwtException e){
            //access토큰 재발급과정 넣기
            reissueAccessToken(request, response, e);
        } catch (UserException e) {
            // JwtUtils에서 던진 UserAccountException 처리
            handleJwtException(response, e);
        } catch (ServletException e) {
            // Spring Security 예외 처리
            handleJwtException(response, new UserException(ErrorCode.UNAUTHORIZED, e.getMessage()));
        }
        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request, String headerName) {
        String headerValue = request.getHeader(headerName);
        if (headerValue != null && headerValue.startsWith("Bearer ")) {
            return headerValue.substring(7);
        } else {
            return null;
        }
    }

    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            String refreshToken = parseBearerToken(request, "Refreshtoken");
            if (refreshToken == null) {
                throw exception;
            }
            String oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION);
            //Try Catch 연장선
            refreshTokenService.validateRefreshToken(refreshToken,oldAccessToken);
            String newAccessToken = refreshTokenService.recreateAccessToken(oldAccessToken);
            String newRefreshToken = refreshTokenService.recreateRefreshToken(refreshToken,newAccessToken);
            String authority = JwtUtils.getAuthority(newAccessToken, secretKey);
            if (authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_AUTHUSER") || authority.equals("ROLE_KAKAO")){
                String userEmail = JwtUtils.getUserEmail(newAccessToken, secretKey);
                if (userEmail != null) {
                    Optional<User> result = userRepository.findByEmail(userEmail);
                    if (result.isPresent()) {
                        User user = result.get();

                        // 인가하는 코드
                        AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user, newAccessToken, user.getAuthorities());
                        authenticated.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticated);
                        response.setHeader("new-access-token", newAccessToken);
                        response.setHeader("new-refresh-token", newRefreshToken);
                    }
                }
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
    }
}