package warehouse.orderline.model;

import warehouse.base.BaseEntity;
import warehouse.items.model.ItemEntity;
import warehouse.orders.model.OrderEntity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
public class OrderLineEntity extends BaseEntity {

    private ItemEntity item;
    private int quantity;
    private BigDecimal subtotal;
    private OrderEntity order;

    @ManyToOne
    public ItemEntity getItem() {
        return item;
    }

    public void setItem(ItemEntity item) {
        this.item = item;
    }

    @Column(name = "quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Column(name = "subtotal")
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @ManyToOne()
    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

}
