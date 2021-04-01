package warehouse.addresses.model;

import warehouse.validated.OnCreate;
import warehouse.validated.OnCreateOrder;
import warehouse.validated.OnUpdate;
import warehouse.validated.OnUpdateOrder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

public class AddressServiceModel {

    private Long id;
    private String region;
    private String city;
    private String street;
    private String phone;

    @Null(groups = OnCreate.class)
    @NotNull(groups = {OnUpdate.class, OnCreateOrder.class, OnUpdateOrder.class})
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Size(min = 3, message = "Region must be at least 3 characters long")
    @NotBlank
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Size(min = 3, message = "City must be at least 3 characters long")
    @NotBlank
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Size(min = 3, message = "Street must be at least 3 characters long")
    @NotBlank
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Size(min = 3, message = "Phone number must be at least 4 characters long")
    @NotBlank
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
