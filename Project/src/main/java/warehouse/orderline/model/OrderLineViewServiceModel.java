package warehouse.orderline.model;

import warehouse.items.model.ItemAddServiceModel;
import warehouse.items.model.ItemViewServiceModel;

import java.math.BigDecimal;

public class OrderLineViewServiceModel {
    private Long id;
    private ItemViewServiceModel item;
    private int quantity;
    private BigDecimal subtotal;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemViewServiceModel getItem() {
        return item;
    }

    public void setItem(ItemViewServiceModel item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
