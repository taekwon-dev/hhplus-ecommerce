package kr.hhplus.be.server.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.domain.auth.exception.IllegalTokenException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_HEADER_KEY = "Authentication";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_TYPE = "Bearer";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = extract(request);
        validateUserToken(userId);
        return true;
    }

    private Long extract(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders(AUTHORIZATION);
        while (headers.hasMoreElements()) {
            String headerValue = headers.nextElement();
            if (isBearerHeader(headerValue)) {
                return getAuthHeaderValue(request, headerValue);
            }
        }
        return null;
    }

    private Long getAuthHeaderValue(HttpServletRequest request, String headerValue) {
        String authHeaderValue = headerValue.substring(BEARER_TYPE.length()).trim();
        request.setAttribute(AUTH_HEADER_KEY, authHeaderValue);
        return Long.parseLong(authHeaderValue);
    }

    private boolean isBearerHeader(String headerValue) {
        return headerValue.toLowerCase().startsWith(BEARER_TYPE.toLowerCase());
    }

    private void validateUserToken(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalTokenException();
        }
    }
}
