package warehouse.suppliers.model;

import warehouse.addresses.model.AddressServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class SupplierServiceModel {

    private Long id;
    private String name;
    private String email;
    private boolean isBlocked;
    private AddressServiceModel address;


    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Size(min = 3, message = "Name must be at least 3 characters long")
    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
