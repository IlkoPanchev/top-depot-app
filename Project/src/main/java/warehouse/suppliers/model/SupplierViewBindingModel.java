package warehouse.suppliers.model;

import warehouse.addresses.model.AddressViewBindingModel;

public class SupplierViewBindingModel {

    private Long id;
    private String name;
    private String email;
    private boolean isBlocked;
    private AddressViewBindingModel address;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public AddressViewBindingModel getAddress() {
        return address;
    }

    public void setAddress(AddressViewBindingModel address) {
        this.address = address;
    }
}
