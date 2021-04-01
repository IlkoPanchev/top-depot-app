package warehouse.orderline;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import warehouse.addresses.model.AddressEntity;
import warehouse.items.model.ItemEntity;
import warehouse.items.service.ItemService;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.repository.OrderLineRepository;
import warehouse.orderline.service.OrderLineService;
import warehouse.orderline.service.impl.OrderLineServiceImpl;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderLineServiceUnitTests {

    private OrderLineService orderLineServiceToTest;
    private OrderLineEntity orderLineEntity;

    @Mock
    OrderLineRepository mockOrderLineRepository;
    @Mock
    ItemService mockItemService;

    @BeforeEach
    public void setUp(){
        this.orderLineServiceToTest = new OrderLineServiceImpl(mockOrderLineRepository, mockItemService);
        this.orderLineEntity = this.createExistingOrderLineEntity();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethod(){

        when(mockOrderLineRepository.findById(any(Long.class))).thenReturn(Optional.of(orderLineEntity));

        OrderLineEntity existingOrderLineEntity = this.orderLineServiceToTest.getById(1L);


        Assertions.assertEquals(2L, existingOrderLineEntity.getQuantity());
        Assertions.assertEquals( new BigDecimal("100"), existingOrderLineEntity.getSubtotal());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethodThrowsEntityNotFoundException(){

        when(mockOrderLineRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.orderLineServiceToTest.getById(2L));

    }

    private OrderLineEntity createExistingOrderLineEntity(){

        OrderLineEntity orderLineEntity = new  OrderLineEntity();
        orderLineEntity.setId(1L);
        ItemEntity itemEntity = new ItemEntity();
        orderLineEntity.setItem(itemEntity);
        orderLineEntity.setQuantity(2);
        orderLineEntity.setSubtotal(new BigDecimal("100"));


        return orderLineEntity;

    }

}
