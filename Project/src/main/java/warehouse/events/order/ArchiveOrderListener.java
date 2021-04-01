package warehouse.events.order;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import warehouse.orders.service.OrderService;

import java.io.IOException;

@Component
public class ArchiveOrderListener {

    private final OrderService orderService;

    public ArchiveOrderListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener(ArchiveOrderEvent.class)
    public void onOrderArchived(ArchiveOrderEvent archiveOrderEvent) throws IOException {
        this.orderService.exportArchivedOrder(archiveOrderEvent.getOrder());
    }
}
