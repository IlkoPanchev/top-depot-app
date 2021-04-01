package warehouse.orders.model;

import warehouse.orderline.model.OrderLineEntity;
import warehouse.base.BaseEntity;
import warehouse.customers.model.CustomerEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CustomerEntity customer;
    private Set<OrderLineEntity> orderLineEntities = new HashSet<>();
    private boolean isClosed;
    private boolean isArchives;
    private boolean isDeleted;
    private BigDecimal total;



    @Column(name = "created_on", nullable = false)
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }


    @Column(name = "updated_on", nullable = false)
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "orders_order_lines",
            joinColumns = @JoinColumn(name = "order_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "order_line_id", referencedColumnName = "id"))
    @OrderBy(value = "id ASC")
    public Set<OrderLineEntity> getOrderLineEntities() {
        return orderLineEntities;
    }

    public void setOrderLineEntities(Set<OrderLineEntity> orderLineEntities) {
        this.orderLineEntities = orderLineEntities;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    @Column(name = "is_closed")
    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    @Column(name = "is_archives")
    public boolean isArchives() {
        return isArchives;
    }

    public void setArchives(boolean archives) {
        isArchives = archives;
    }

    @Column(name = "is_deleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Column(name = "total")
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

//    @PrePersist
//   public void prePersist(){
//       setCreatedOn(LocalDateTime.now());
//       setUpdatedOn(LocalDateTime.now());
//   }
//
//    @PreUpdate
//    public void preUpdate(){
//        setUpdatedOn(LocalDateTime.now());
//    }
}
