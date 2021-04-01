package warehouse.orderline.model;

import com.google.gson.annotations.Expose;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemViewBindingModel;

import java.math.BigDecimal;

public class OrderLineViewBindingModel {
    @Expose
    private Long id;
    @Expose
    private ItemViewBindingModel item;
    @Expose
    private int quantity;
    @Expose
    private BigDecimal subtotal;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemViewBindingModel getItem() {
        return item;
    }

    public void setItem(ItemViewBindingModel item) {
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
