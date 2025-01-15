package kr.hhplus.be.server.config.web;

import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.global.auth.AuthInterceptor;
import kr.hhplus.be.server.global.auth.UserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserService userService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserArgumentResolver(userService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/v1/products/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/swagger-config",
                        "/static/swagger-ui/openapi3.yaml"
                );
    }
}
