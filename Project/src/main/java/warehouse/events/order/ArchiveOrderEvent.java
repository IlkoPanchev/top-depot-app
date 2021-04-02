package warehouse.events.order;

import org.springframework.context.ApplicationEvent;
import warehouse.orders.model.OrderViewBindingModel;

public class ArchiveOrderEvent extends ApplicationEvent {

    private OrderViewBindingModel order;

    public ArchiveOrderEvent(Object source, OrderViewBindingModel orderViewBindingModel) {
        super(source);
        this.order = orderViewBindingModel;
    }

    public OrderViewBindingModel getOrder() {
        return order;
    }

    public void setOrder(OrderViewBindingModel order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return String.format("ArchiveOrderЕvent - order %s", order.getId());
    }
}
