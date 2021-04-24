package warehouse.orderline.service;

import warehouse.orderline.model.OrderLineEntity;

import java.util.Optional;

public interface OrderLineService {

    void initOrderLines();

    OrderLineEntity getById(long id);

    Optional<OrderLineEntity> findById(long id);
}
