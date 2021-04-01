package warehouse.orders.model;

import warehouse.customers.model.CustomerServiceModel;
import warehouse.orderline.model.OrderLineAddBindingModel;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.model.OrderLineViewBindingModel;
import warehouse.orderline.model.OrderLineViewServiceModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class OrderViewServiceModel {
    private Long id;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CustomerServiceModel customer;
    private Set<OrderLineViewServiceModel> orderLineEntities = new HashSet<>();
    private boolean isClosed;
    private boolean isArchives;
    private boolean isDeleted;
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

    public CustomerServiceModel getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerServiceModel customer) {
        this.customer = customer;
    }

    public Set<OrderLineViewServiceModel> getOrderLineEntities() {
        return orderLineEntities;
    }

    public void setOrderLineEntities(Set<OrderLineViewServiceModel> orderLineEntities) {
        this.orderLineEntities =  orderLineEntities;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
