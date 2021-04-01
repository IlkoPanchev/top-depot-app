package warehouse.orders.model;

import warehouse.customers.model.CustomerAddBindingModel;
import warehouse.orderline.model.OrderLineAddBindingModel;
import warehouse.orderline.model.OrderLineAddServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class OrderAddBindingModel {

    private Long id;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CustomerAddBindingModel customer;
    private Set<OrderLineAddBindingModel> orderLineEntities = new HashSet<>();
    private boolean isClosed;
    private boolean isArchives;
    private boolean isDeleted;
    private BigDecimal total;


    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
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

    @Valid
    public CustomerAddBindingModel getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerAddBindingModel customer) {
        this.customer = customer;
    }

    @Valid
    public Set<OrderLineAddBindingModel> getOrderLineEntities() {
        return orderLineEntities;
    }

    public void setOrderLineEntities(Set<OrderLineAddBindingModel> orderLineEntities) {
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @DecimalMin(value = "0", message = "Total should be positive")
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
