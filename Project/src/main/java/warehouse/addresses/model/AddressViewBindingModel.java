package warehouse.addresses.model;

import com.google.gson.annotations.Expose;

public class AddressViewBindingModel {

    @Expose
    private Long id;
    @Expose
    private String region;
    @Expose
    private String city;
    @Expose
    private String street;
    @Expose
    private String phone;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
