package warehouse.statistics;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
@Service
public class StatsService {

    private AtomicInteger requestCount = new AtomicInteger(0);
    private AtomicInteger errorsCount = new AtomicInteger(0);
    private LocalDateTime startedOn = LocalDateTime.now();

    public void incRequestsCount() {
        requestCount.incrementAndGet();
    }

    public int getRequestsCount() {
        return requestCount.intValue();
    }

    public void incErrorsCount (){
         errorsCount.incrementAndGet();
    }

    public int getErrorsCount(){
        return errorsCount.intValue();
    }

    public LocalDateTime getStartedOn() {
        return startedOn;
    }
}
