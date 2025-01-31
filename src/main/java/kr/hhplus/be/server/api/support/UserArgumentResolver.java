package kr.hhplus.be.server.api.support;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.domain.user.domain.User;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static kr.hhplus.be.server.api.support.interceptor.AuthInterceptor.AUTH_HEADER_KEY;

@RequiredArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(User.class) && isController(parameter);
    }

    private boolean isController(MethodParameter parameter) {
        return parameter.getDeclaringClass().isAnnotationPresent(RestController.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Long userId = Long.parseLong((String) request.getAttribute(AUTH_HEADER_KEY));
        return userService.findUserById(userId);
    }
}
