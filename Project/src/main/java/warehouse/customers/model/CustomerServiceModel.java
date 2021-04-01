package warehouse.customers.model;

import warehouse.addresses.model.AddressServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnCreateOrder;
import warehouse.validated.OnUpdate;
import warehouse.validated.OnUpdateOrder;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class CustomerServiceModel {

    private Long id;
    private String companyName;
    private String personName;
    private String email;
    private boolean isBlocked;
    private AddressServiceModel address;


    @Null(groups = OnCreate.class)
    @NotNull(groups = {OnUpdate.class, OnCreateOrder.class, OnUpdateOrder.class})
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Size(min = 3, message = "Name must be at least 3 character long")
    @NotBlank
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Size(min = 3, message = "Name must be at least 3 character long")
    @NotBlank
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Email(message = "Enter valid email")
    @NotBlank
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

    @Valid
    public AddressServiceModel getAddress() {
        return address;
    }

    public void setAddress(AddressServiceModel address) {
        this.address = address;
    }
}
