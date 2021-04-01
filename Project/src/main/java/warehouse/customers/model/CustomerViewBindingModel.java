package warehouse.customers.model;

import com.google.gson.annotations.Expose;
import warehouse.addresses.model.AddressViewBindingModel;

public class CustomerViewBindingModel {

    @Expose
    private Long id;
    @Expose
    private String companyName;
    @Expose
    private String personName;
    @Expose
    private String email;
    @Expose
    private boolean isBlocked;
    @Expose
    private AddressViewBindingModel address;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
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
