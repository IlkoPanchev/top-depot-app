package warehouse.customers.model;

import org.hibernate.validator.constraints.Length;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class CustomerAddBindingModel {

    private Long id;
    private String companyName;
    private String personName;
    private String email;
    private AddressAddBindingModel address;


    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
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

    @Valid
    public AddressAddBindingModel getAddress() {
        return address;
    }

    public void setAddress(AddressAddBindingModel address) {
        this.address = address;
    }
}
