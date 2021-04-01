package warehouse.orders.orderdata;

import warehouse.customers.model.CustomerViewBindingModel;
import warehouse.items.model.ItemViewBindingModel;
import warehouse.orderline.model.OrderLineViewBindingModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class OrderData {

    private Long id;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CustomerViewBindingModel customer;
    private Set<OrderLineViewBindingModel> orderLineEntities = new TreeSet<>(Comparator
            .comparing((OrderLineViewBindingModel o) -> o.getItem().getName()));
    private boolean isClosed;
    private boolean isArchives;
    private BigDecimal total;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public CustomerViewBindingModel getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerViewBindingModel customer) {
        this.customer = customer;
    }

    public Set<OrderLineViewBindingModel> getOrderLineEntities() {
        return orderLineEntities;
    }

    public void setOrderLineEntities(Set<OrderLineViewBindingModel> orderLineEntities) {
        this.orderLineEntities = orderLineEntities;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isArchives() {
        return isArchives;
    }

    public void setArchives(boolean archives) {
        isArchives = archives;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderLineViewBindingModel getOrderLine(Long id) {
        for (OrderLineViewBindingModel orderLineEntity : this.orderLineEntities) {
            if (orderLineEntity.getItem().getId().equals(id)) {
                return orderLineEntity;
            }
        }
        return null;
    }

    public int getOrderLineEntityCount() {
        return this.orderLineEntities.size();
    }

    public void addOrderLine(ItemViewBindingModel itemViewBindingModel, int quantity) {

        OrderLineViewBindingModel orderLineEntity = getOrderLine(itemViewBindingModel.getId());

        if (orderLineEntity != null) {
            orderLineEntity.setQuantity(orderLineEntity.getQuantity() + quantity);
            BigDecimal subtotal = calculateSubtotal(orderLineEntity.getItem().getPrice(), orderLineEntity.getQuantity());
            orderLineEntity.setSubtotal(subtotal);
        } else {
            orderLineEntity = new OrderLineViewBindingModel();

            orderLineEntity.setItem(itemViewBindingModel);
            orderLineEntity.setQuantity(quantity);
            BigDecimal subtotal = calculateSubtotal(orderLineEntity.getItem().getPrice(), orderLineEntity.getQuantity());
            orderLineEntity.setSubtotal(subtotal);
            this.orderLineEntities.add(orderLineEntity);
        }
    }

    private BigDecimal calculateSubtotal(BigDecimal price, int quantity) {
        BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
        return subtotal;
    }


    public void updateOrderLine(Long id, int quantity) {

        OrderLineViewBindingModel orderLineEntity = getOrderLine(id);

        if (orderLineEntity != null) {
            orderLineEntity.setQuantity(quantity);
            BigDecimal subtotal = calculateSubtotal(orderLineEntity.getItem().getPrice(), orderLineEntity.getQuantity());
            orderLineEntity.setSubtotal(subtotal);
        }
    }

    public void removeOrderLine(Long id) { // throws ProductNotFoundException

        OrderLineViewBindingModel orderLineEntity = getOrderLine(id);

        if (orderLineEntity != null) {
            this.orderLineEntities.remove(orderLineEntity);
        }
    }

    public void clear() {
        this.orderLineEntities.clear();
        this.total = new BigDecimal('0');
    }

    public BigDecimal getTotalAmount() {

        total = new BigDecimal("0");

        for (OrderLineViewBindingModel orderLineEntity : this.orderLineEntities) {
            BigDecimal price = orderLineEntity.getItem().getPrice();
            BigDecimal quantity = new BigDecimal(orderLineEntity.getQuantity());
            BigDecimal amount = price.multiply(quantity);
            total = total.add(amount);
        }
        return total;
    }
}
