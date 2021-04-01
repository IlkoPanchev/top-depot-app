package warehouse.items.model;

import org.springframework.web.multipart.MultipartFile;
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.items.validation.ValidCategory;
import warehouse.items.validation.ValidFile;
import warehouse.items.validation.ValidSupplier;
import warehouse.orders.model.OrderEntity;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnCreateOrder;
import warehouse.validated.OnUpdate;
import warehouse.validated.OnUpdateOrder;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class ItemAddServiceModel {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String location;
    private String category;
    private String supplier;
    private Set<OrderEntity> orders = new HashSet<>();
    private MultipartFile img;

    @Null(groups = OnCreate.class)
    @NotNull(groups = {OnUpdate.class, OnCreateOrder.class, OnUpdateOrder.class})
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Size(min = 3, message = "Name must be at least 3 characters long")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DecimalMin(value = "0", message = "Price must be positive number")
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @NotBlank(message = "Enter location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Set<OrderEntity> getOrders() {
        return orders;
    }

    public void setOrders(Set<OrderEntity> orders) {
        this.orders = orders;
    }

    @ValidFile(message = "Select image")
    public MultipartFile getImg() {
        return img;
    }

    public void setImg(MultipartFile img) {
        this.img = img;
    }
}
