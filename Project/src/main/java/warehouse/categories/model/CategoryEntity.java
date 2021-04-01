package warehouse.categories.model;

import warehouse.base.BaseEntity;
import warehouse.items.model.ItemEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    private String name;
    private String description;
    private boolean isBlocked;
    private Set<ItemEntity> items = new HashSet<>();

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

    @Column(name = "is_blocked", nullable = false)
    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
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
        itemEntity.setCategory(this);
    }

    public void removeItem(ItemEntity itemEntity){
        if (!this.items.contains(itemEntity)){
            return;
        }
        this.items.remove(itemEntity);
        itemEntity.setCategory(null);
    }

    public void removeItems(){
        this.items.forEach(itemEntity -> itemEntity.setCategory(null));
        this.items.clear();
    }
}
