package warehouse.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import warehouse.statistics.StatsInterceptor;
import warehouse.statistics.UnauthorizedErrorInterceptor;

@Component
public class WebConfig implements WebMvcConfigurer {

    private StatsInterceptor statsInterceptor;
    private UnauthorizedErrorInterceptor unauthorizedErrorInterceptor;

    public WebConfig(StatsInterceptor statsInterceptor,
                     UnauthorizedErrorInterceptor unauthorizedErrorInterceptor) {
        this.statsInterceptor = statsInterceptor;
        this.unauthorizedErrorInterceptor = unauthorizedErrorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(statsInterceptor);
        registry.addInterceptor(unauthorizedErrorInterceptor);
    }
}
