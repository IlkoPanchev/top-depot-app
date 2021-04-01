package warehouse.orders.shedule;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;

@Component
public class OrdersCleanupScheduler {

    private final OrderService orderService;
    private static final Logger logger = Logger.getLogger(OrdersCleanupScheduler.class);

    public OrdersCleanupScheduler(OrderService orderService) {

        this.orderService = orderService;
    }


    @Scheduled(cron = "${spring.cron}")
    public void cleanUpOldOrders() {
        LocalDateTime upTo = LocalDateTime.now().minus(168, HOURS);
        List<OrderViewServiceModel> orderViewServiceModels = this.orderService
                .getAllByUpdatedOnBeforeAndClosedFalseAndArchivesFalse(upTo);
        if (!orderViewServiceModels.isEmpty()) {
            for (OrderViewServiceModel order : orderViewServiceModels) {
                logger.info(String.format("Deleted order %s", order.getId()));
                this.orderService.markAsDeleted(order.getId());
            }
        }
        else {
            logger.info("No old orders");
        }

    }


}
