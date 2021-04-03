package warehouse.users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;
import warehouse.users.model.UserEntity;
import warehouse.users.service.UserService;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl serviceToTest;

    private String TEST_USER_NAME_EXISTS = "admin";
    private String TEST_USER_NAME_NOT_EXISTS = "xxx";

    private UserEntity testUserEntity;

    private RoleEntity testRoleEntity;

    @Mock
    private UserService mockUserService;


    @BeforeEach
    public void setUp() {
        testRoleEntity = new RoleEntity();
        testRoleEntity.setRole(RoleName.ROLE_ADMIN);

        testUserEntity = new UserEntity();
        testUserEntity.setUsername("admin");
        testUserEntity.setEmail("admin@example.com");
        testUserEntity.setPassword("aaa");
        testUserEntity.setRoles(Set.of(testRoleEntity));

        serviceToTest = new UserDetailsServiceImpl(mockUserService);
    }

    @Test
    public void testUserNotFound() {

        when(mockUserService.findUserByUsername(TEST_USER_NAME_NOT_EXISTS)).
                thenReturn(Optional.empty());

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            serviceToTest.loadUserByUsername(TEST_USER_NAME_NOT_EXISTS);
        });
    }

    @Test
    public void testUserFound() {

        when(mockUserService.findUserByUsername(TEST_USER_NAME_EXISTS)).
                thenReturn(Optional.of(testUserEntity));

        UserDetails actualDetails = serviceToTest.loadUserByUsername(TEST_USER_NAME_EXISTS);

        Assertions.assertEquals(testUserEntity.getUsername(), actualDetails.getUsername());
    }


}
