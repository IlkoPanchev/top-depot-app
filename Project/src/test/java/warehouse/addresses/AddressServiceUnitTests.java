package warehouse.addresses;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.repository.AddressRepository;
import warehouse.addresses.service.AddressService;
import warehouse.addresses.service.impl.AddressServiceImpl;
import warehouse.categories.model.CategoryEntity;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddressServiceUnitTests {

    private AddressService addressServiceToTest;
    private AddressEntity addressEntity;

    @Mock
    AddressRepository mockAddressRepository;

    @BeforeEach
    public void setUp(){

        this.addressServiceToTest = new AddressServiceImpl(mockAddressRepository);
        this.addressEntity = this.createExistingAddressEntity();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethod(){

        when(mockAddressRepository.findById(any(Long.class))).thenReturn(Optional.of(addressEntity));

        AddressEntity existingAddress = this.addressServiceToTest.getById(1L);


        Assertions.assertEquals(existingAddress.getId(), 1L);
        Assertions.assertEquals(existingAddress.getCity(), "city");

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethodThrowsEntityNotFoundException(){

        when(mockAddressRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.addressServiceToTest.getById(2L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddressExistsMethodReturnsTrueWhenAddressIsPresent(){

        when(mockAddressRepository.findById(any(Long.class))).thenReturn(Optional.of(addressEntity));

        boolean result = this.addressServiceToTest.addressExists(1L);

        Assertions.assertTrue(result);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddressExistsMethodReturnsFalseWhenAddressDoesntExists(){

        when(mockAddressRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        boolean result = this.addressServiceToTest.addressExists(1L);

        Assertions.assertFalse(result);

    }

    private AddressEntity createAddressEntity() {

        AddressEntity addressEntity = new  AddressEntity();
        addressEntity.setRegion("region");
        addressEntity.setCity("city");
        addressEntity.setStreet("street");
        addressEntity.setPhone("111222333");

        return addressEntity;
    }

    private AddressEntity createExistingAddressEntity(){

        AddressEntity addressEntity = this.createAddressEntity();
        addressEntity.setId(1L);

        return addressEntity;

    }
}
