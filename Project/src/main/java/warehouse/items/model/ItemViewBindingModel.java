package warehouse.items.model;

import com.google.gson.annotations.Expose;

import java.math.BigDecimal;

public class ItemViewBindingModel {


    @Expose
    private Long id;
    @Expose
    private String name;
    @Expose
    private String description;
    @Expose
    private BigDecimal price;
    @Expose
    private int stock;
    @Expose
    private String location;
    @Expose
    private String category;
    @Expose
    private String supplier;
    @Expose
    private boolean isBlocked;
    @Expose
    private String img;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public ItemViewBindingModel setStock(int stock) {
        this.stock = stock;
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

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

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
