package warehouse.suppliers.model;

import warehouse.base.BaseEntity;
import warehouse.addresses.model.AddressEntity;
import warehouse.items.model.ItemEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "suppliers")
public class SupplierEntity extends BaseEntity {

    private String name;
    private String email;
    private AddressEntity addressEntity;
    private boolean isBlocked;
    private Set<ItemEntity> items = new HashSet<>();

    @Column(name = "name", nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "email", nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(mappedBy = "supplier", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    public Set<ItemEntity> getItems() {
        return items;
    }

    public void setItems(Set<ItemEntity> items) {
        this.items = items;
    }

    public void addItem(ItemEntity itemEntity){
        if (this.items.contains(itemEntity)){
            return;
        }
        this.items.add(itemEntity);
        itemEntity.setSupplier(this);
    }

    public void removeItem(ItemEntity itemEntity){
        if (!this.items.contains(itemEntity)){
            return;
        }
        this.items.remove(itemEntity);
        itemEntity.setSupplier(null);
    }

    public void removeItems(){
        this.items.forEach(itemEntity -> itemEntity.setSupplier(null));
        this.items.clear();
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    public AddressEntity getAddressEntity() {
        return addressEntity;
    }

    public void setAddressEntity(AddressEntity addressEntity) {
        this.addressEntity = addressEntity;
    }

    @Column(name = "is_blocked")
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
