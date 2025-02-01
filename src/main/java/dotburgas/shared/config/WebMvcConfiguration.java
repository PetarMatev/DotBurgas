package dotburgas.shared.config;

import dotburgas.shared.security.SessionCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SessionCheckInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //** - everything after
        registry.addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/img/**", "/js/**", "/static/**", "/webjars/**");

    }
}
