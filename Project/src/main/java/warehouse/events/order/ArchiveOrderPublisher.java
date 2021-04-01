package warehouse.events.order;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import warehouse.orders.model.OrderViewBindingModel;

@Component
public class ArchiveOrderPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ArchiveOrderPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishOrderArchived(OrderViewBindingModel orderViewBindingModel){
        ArchiveOrderEvent archiveOrderEvent = new ArchiveOrderEvent(this, orderViewBindingModel);
        this.applicationEventPublisher.publishEvent(archiveOrderEvent);
    }
}
