package warehouse.roles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;
import warehouse.roles.repository.RoleRepository;
import warehouse.roles.service.RoleService;
import warehouse.roles.service.impl.RoleServiceImpl;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RolesServiceUnitTests {

    private RoleEntity roleEntity;

    private RoleService roleServiceToTest;

    @Mock
    RoleRepository mockRoleRepository;

    @BeforeEach
    public void SetUp(){
        this.roleServiceToTest = new RoleServiceImpl(mockRoleRepository);
        this.roleEntity = this.createExistingRoleEntity();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByNameMethod(){

        when(this.mockRoleRepository.findByRole(any(RoleName.class))).thenReturn(Optional.of(roleEntity));

        RoleEntity existingRoleEntity = this.roleServiceToTest.getByName(RoleName.ROLE_MANAGER);

        Assertions.assertEquals(RoleName.ROLE_MANAGER, existingRoleEntity.getRole());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByNameMethodThrowsEntityNotFoundException(){

        when(this.mockRoleRepository.findByRole(any(RoleName.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.roleServiceToTest.getByName(RoleName.ROLE_MANAGER));
    }

    private RoleEntity createExistingRoleEntity() {

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setRole(RoleName.ROLE_MANAGER);
        return roleEntity;
    }
}
