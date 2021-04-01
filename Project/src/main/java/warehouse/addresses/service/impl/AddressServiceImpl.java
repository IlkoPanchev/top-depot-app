package warehouse.addresses.service.impl;

import org.springframework.stereotype.Service;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.repository.AddressRepository;
import warehouse.addresses.service.AddressService;
import warehouse.categories.model.CategoryEntity;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static warehouse.constants.GlobalConstants.*;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }


    @Override
    public void initAddresses() {

        if (this.addressRepository.count() == 0) {
            for (int i = 1; i < INIT_COUNT; i++) {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setRegion(String.format("Region_%d", i));
                addressEntity.setCity(String.format("City_%d", i));
                addressEntity.setStreet(String.format("Street_street_%d", i));
                addressEntity.setPhone(String.format("+359 882 12345%d", i));

                this.addressRepository.saveAndFlush(addressEntity);

            }
        }
    }

    @Override
    public AddressEntity getById(long id) {

        AddressEntity addressEntity = this.addressRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found address with id: " + id));

        return addressEntity;
    }

    @Override
    public boolean addressExists(long id) {

        Optional<AddressEntity> addressEntity = this.addressRepository.findById(id);

        return addressEntity.isPresent();
    }
}
