package warehouse.orderline.service.impl;

import org.springframework.stereotype.Service;
import warehouse.items.model.ItemEntity;
import warehouse.items.service.ItemService;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.repository.OrderLineRepository;
import warehouse.orderline.service.OrderLineService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import java.math.BigDecimal;

import static warehouse.constants.GlobalConstants.*;

@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final ItemService itemService;

    public OrderLineServiceImpl(OrderLineRepository orderLineRepository, ItemService itemService) {
        this.orderLineRepository = orderLineRepository;
        this.itemService = itemService;
    }

    @Transactional
    @Override
    public void initOrderLines() {

        if (this.orderLineRepository.count() == 0){

            for (int i = 1; i < INIT_COUNT; i++) {

                OrderLineEntity orderLineEntity = new OrderLineEntity();
                ItemEntity itemEntity = this.itemService.getById(i);
                orderLineEntity.setItem(itemEntity);
                orderLineEntity.setQuantity(i);
                orderLineEntity.setSubtotal(itemEntity.getPrice().multiply(new BigDecimal(i)));

                this.orderLineRepository.saveAndFlush(orderLineEntity);

            }


        }

    }

    @Override
    public OrderLineEntity getById(long id) {

        OrderLineEntity orderLineEntity = this.orderLineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found order line with id: " + id));

        return orderLineEntity;
    }
}
