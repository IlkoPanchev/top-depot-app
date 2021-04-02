package warehouse.orderline.service;

import warehouse.orderline.model.OrderLineEntity;

public interface OrderLineService {

    void initOrderLines();

    OrderLineEntity getById(long id);
}
