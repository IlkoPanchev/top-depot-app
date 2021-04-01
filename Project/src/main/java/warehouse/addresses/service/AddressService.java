package warehouse.addresses.service;

import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.model.AddressServiceModel;

public interface AddressService {

    void initAddresses();

    AddressEntity getById(long id);

    boolean addressExists(long id);
}
