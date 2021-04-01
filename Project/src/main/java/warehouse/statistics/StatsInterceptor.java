package warehouse.statistics;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StatsInterceptor implements HandlerInterceptor {

    private final StatsService statsService;
    private static final Logger logger = Logger.getLogger(StatsInterceptor.class);

    @Autowired
    public StatsInterceptor(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        statsService.incRequestsCount();
        logger.info("[preHandle][" + request + "]" + "[" + request.getMethod()
                + "]" + "[" + request.getRequestURI() + "]");

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,Object handler, Exception ex) throws Exception {

        if (ex != null){
            statsService.incErrorsCount();
            logger.error("[preHandle][" + request + "]" + "[" + request.getMethod()
                    + "][" + request.getRequestURI() + "][exception: " + ex + "]");
        }

    }

}
