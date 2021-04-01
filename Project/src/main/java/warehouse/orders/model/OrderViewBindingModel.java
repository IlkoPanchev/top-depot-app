package warehouse.orders.model;

import com.google.gson.annotations.Expose;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerViewBindingModel;
import warehouse.orderline.model.OrderLineAddBindingModel;
import warehouse.orderline.model.OrderLineViewBindingModel;
import warehouse.orderline.model.OrderLineViewServiceModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class OrderViewBindingModel {
    @Expose
    private Long id;
    @Expose
    private LocalDateTime createdOn;
    @Expose
    private LocalDateTime updatedOn;
    @Expose
    private CustomerViewBindingModel customer;
    @Expose
    private Set<OrderLineViewBindingModel> orderLineEntities =  new HashSet<>();
    @Expose
    private boolean isClosed;
    @Expose
    private boolean isArchives;
    @Expose
    private boolean isDeleted;
    @Expose
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
