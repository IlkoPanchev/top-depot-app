package warehouse.items.model;

import warehouse.base.BaseEntity;
import warehouse.categories.model.CategoryEntity;
import warehouse.suppliers.model.SupplierEntity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class ItemEntity extends BaseEntity {

    private String name;
    private String description;
    private BigDecimal price;
    private String location;
    private CategoryEntity category;
    private SupplierEntity supplier;
    private boolean isBlocked;
    private String img;

    @Column(name = "name", nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "description", columnDefinition = "TEXT")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "price", nullable = false)
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column(name = "location", nullable = false)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="supplier_id", referencedColumnName = "id", nullable = false)
    public SupplierEntity getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierEntity supplier) {
        this.supplier = supplier;
    }

    @Column(name = "is_blocked", nullable = false)
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Column(name = "img")
    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    //    @ManyToMany(mappedBy = "items", targetEntity = OrderEntity.class, fetch = FetchType.EAGER)
//    @ManyToMany(mappedBy = "items", fetch = FetchType.LAZY)
//
//    public Set<OrderEntity> getOrders() {
//        return orders;
//    }
//
//    public void setOrders(Set<OrderEntity> orders) {
//        this.orders = orders;
//    }

//    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
//    public List<OrderLine> getOrderLines() {
//        return orderLines;
//    }
//
//    public void setOrderLines(List<OrderLine> orderLines) {
//        this.orderLines = orderLines;
//    }




}
