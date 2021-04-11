package warehouse.statistics;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class UnauthorizedErrorInterceptor implements HandlerInterceptor {

    private final UnauthorizedErrorService unauthorizedErrorService;

    public UnauthorizedErrorInterceptor(UnauthorizedErrorService unauthorizedErrorService) {
        this.unauthorizedErrorService = unauthorizedErrorService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        int status = response.getStatus();


            if (ex != null && ("Access is denied").equals(ex.getMessage())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentPrincipalName = authentication.getName();
                UnauthorizedError unauthorizedError = new UnauthorizedError(request.getRequestURI(), currentPrincipalName, LocalDateTime.now());

                this.unauthorizedErrorService.getUnauthorizedErrorList().add(unauthorizedError);
                String debug = "";
            }


    }

}
