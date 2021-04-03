package warehouse.users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;
import warehouse.departments.model.DepartmentServiceModel;
import warehouse.departments.service.DepartmentService;
import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;
import warehouse.roles.service.RoleService;
import warehouse.users.model.UserEntity;
import warehouse.users.model.UserServiceModel;
import warehouse.users.repository.UserRepository;
import warehouse.users.service.UserService;
import warehouse.users.service.impl.UserServiceImpl;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {

    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String PASSWORD_ENCODED = "password_encoded";
    private final String ROLE_MANAGER = "ROLE_MANAGER";
    private final String ROLE_USER = "ROLE_USER";
    private final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

    private UserService userServiceToTest;
    private UserEntity userEntity;
    private UserServiceModel userServiceModel;
    private UserServiceModel existingUserServiceModel;
    private DepartmentEntity departmentEntity;
    private Pageable pageable;

    @Mock
    UserRepository mockUserRepository;
    @Mock
    DepartmentService mockDepartmentService;
    @Mock
    ValidationUtil mockValidationUtil;
    @Mock
    BCryptPasswordEncoder mockBCryptPasswordEncoder;
    @Mock
    RoleService mockRoleService;

    @BeforeEach
    public void setUp(){
        this.userServiceToTest = new UserServiceImpl(mockUserRepository,
                new ModelMapper(),
                mockDepartmentService,
                mockBCryptPasswordEncoder,
                mockValidationUtil,
                mockRoleService);
        this.userEntity = this.createExistingUserEntity();
        this.userServiceModel = this.createUserServiceModel();
        this.existingUserServiceModel = this.createExistingUserServiceModel();
        this.departmentEntity = this.createExistingDepartmentEntity();
        this.pageable = this.initPageable();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindUserByUsernameMethod(){

        when(mockUserRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));

        Optional<UserEntity> existingUserEntity = this.userServiceToTest.findUserByUsername(USERNAME);

        Assertions.assertEquals(existingUserEntity.get().getPassword(), PASSWORD);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetUserByUserName(){

        when(mockUserRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));

        UserServiceModel existingUserServiceModel = this.userServiceToTest.getUserByUserName(USERNAME);

        Assertions.assertEquals(existingUserServiceModel.getPassword(), userEntity.getPassword());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetUserByUserNameThrowsEntityNotFoundException(){

        when(mockUserRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.userServiceToTest.getUserByUserName(USERNAME));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testRegisterMethodWithValidServiceModel(){

        when(mockValidationUtil.isValid(any(UserServiceModel.class))).thenReturn(true);
        when(mockUserRepository.saveAndFlush(any(UserEntity.class))).thenReturn(userEntity);
        when(mockBCryptPasswordEncoder.encode(any(String.class))).thenReturn(PASSWORD_ENCODED);
        when(mockDepartmentService.findByName(any(DepartmentName.class))).thenReturn(departmentEntity);

        userServiceToTest.register(userServiceModel);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity userEntityActual = argument.getValue();

        Assertions.assertEquals(userEntityActual.getUsername(), userServiceModel.getUsername());
        Assertions.assertEquals(userEntityActual.getPassword(), PASSWORD_ENCODED);
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testRegisterMethodThrowsConstraintViolationException() {

        when(mockValidationUtil.isValid(userServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            userServiceToTest.register(userServiceModel);
        });
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddRoleMethod() {


        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

         userServiceToTest.addRole(1L, ROLE_MANAGER);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity userEntityActual = argument.getValue();

        List<String> userEntityActualRoles = userEntityActual.
                getRoles().
                stream().
                map(r -> r.getRole().name()).
                collect(Collectors.toList());

        Assertions.assertTrue(userEntityActualRoles.contains(ROLE_MANAGER));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddRoleMethodThrowsEntityNotFoundException() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, ()-> this.userServiceToTest.addRole(1L, ROLE_MANAGER));
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testRemoveRoleMethod() {

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRole(RoleName.ROLE_MANAGER);
        userEntity.getRoles().add(roleEntity);

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        userServiceToTest.removeRole(1L, ROLE_MANAGER);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity userEntityActual = argument.getValue();

        List<String> userEntityActualRoles = userEntityActual.
                getRoles().
                stream().
                map(r -> r.getRole().name()).
                collect(Collectors.toList());

        Assertions.assertFalse(userEntityActualRoles.contains(ROLE_MANAGER));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testRemoveRoleMethodThrowsEntityNotFoundExceptionForUser() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, ()-> this.userServiceToTest.removeRole(1L, ROLE_MANAGER));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testRemoveRoleMethodThrowsEntityNotFoundExceptionForRole() {

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRole(RoleName.ROLE_MANAGER);
        userEntity.getRoles().add(roleEntity);

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        Assertions.assertThrows(EntityNotFoundException.class, ()-> this.userServiceToTest.removeRole(1L, ROLE_CUSTOMER));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUserExistsMethodReturnsTrue(){

        when(mockUserRepository.findByUsername(any(String.class))).thenReturn(Optional.of(userEntity));

        boolean result  = userServiceToTest.userExists(USERNAME);

        Assertions.assertTrue(result);

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUserExistsMethodReturnsFalse(){

        when(mockUserRepository.findByUsername(any(String.class))).thenReturn(Optional.empty());

        boolean result  = userServiceToTest.userExists(USERNAME);

        Assertions.assertFalse(result);

    }


    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod(){

        Page<UserEntity> userEntities = new PageImpl<>(List.of(userEntity), pageable, 1L);

        when(mockUserRepository.findAll(any(Pageable.class))).thenReturn(userEntities);

        Page<UserServiceModel> userServiceModelPage = userServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(userServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(userServiceModelPage.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindUserByIdMethod() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        UserServiceModel userServiceModel = this.userServiceToTest.findUserById(1L);

        Assertions.assertEquals(userServiceModel.getId(), 1L);
        Assertions.assertEquals(userServiceModel.getUsername(), USERNAME);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindUserByIdMethodThrowsEntityNotFoundException() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, ()-> this.userServiceToTest.findUserById(2L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodWithValidServiceModel() throws Exception {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));
        when(mockValidationUtil.isValid(existingUserServiceModel)).thenReturn(true);
        when(mockUserRepository.saveAndFlush(any(UserEntity.class)))
                .thenReturn(userEntity);

        this.userServiceToTest.edit(existingUserServiceModel);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity newUserActual = argument.getValue();

        Assertions.assertEquals(existingUserServiceModel.getId(), newUserActual.getId());
        Assertions.assertEquals(existingUserServiceModel.getUsername(), newUserActual.getUsername());
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingUserServiceModel)).thenReturn(false);
        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());


        Assertions.assertThrows(ConstraintViolationException.class, () -> this.userServiceToTest.edit(existingUserServiceModel));

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsEntityNotFoundException() throws Exception {

        when(mockValidationUtil.isValid(existingUserServiceModel)).thenReturn(true);
        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.userServiceToTest.edit(existingUserServiceModel));

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethod() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        userServiceToTest.block(1L);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity newUserEntityActual = argument.getValue();

        Assertions.assertFalse(newUserEntityActual.isEnabled());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethodThrowsEntityNotFoundException() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. userServiceToTest.block(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethod() {

        userEntity.setEnabled(false);

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        userServiceToTest.unblock(1L);

        ArgumentCaptor<UserEntity> argument = ArgumentCaptor.forClass(UserEntity.class);
        Mockito.verify(mockUserRepository, times(1)).saveAndFlush(argument.capture());
        UserEntity newUserEntityActual = argument.getValue();

        Assertions.assertTrue(newUserEntityActual.isEnabled());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethodThrowsEntityNotFoundException() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. userServiceToTest.unblock(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetRolesByUserIdMethod() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.of(userEntity));

        Set<String> userRoles = this.userServiceToTest.getRolesByUserId(1L);

        Assertions.assertTrue(userRoles.contains(ROLE_USER));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetRolesByUserIdMethodThrowsEntityNotFoundException() {

        when(mockUserRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. userServiceToTest.getRolesByUserId(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<UserEntity> userEntities = new PageImpl<>(List.of(userEntity), pageable, 1L);

        when(mockUserRepository.search(any(String.class), any(Pageable.class))).thenReturn(userEntities);

        Page<UserServiceModel> userServiceModelPage = userServiceToTest.search(USERNAME, pageable);

        Assertions.assertEquals(userServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(userServiceModelPage.getTotalPages(), 1);
        Assertions.assertEquals(USERNAME, userServiceModelPage.get().collect(Collectors.toList()).get(0).getUsername());
    }

    private Pageable initPageable() {

        String option = "username";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }


    private UserEntity createExistingUserEntity() {

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("username");
        userEntity.setPassword("password");

        DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setDepartmentName(DepartmentName.DEPARTMENT_I);
        userEntity.setDepartment(new DepartmentEntity());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRole(RoleName.ROLE_USER);
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleEntity);
        userEntity.setRoles(roles);


        return userEntity;
    }

    private UserServiceModel createUserServiceModel() {

        UserServiceModel userServiceModel = new UserServiceModel();
        userServiceModel.setUsername("username");
        userServiceModel.setPassword("password");

        DepartmentServiceModel departmentServiceModel = new DepartmentServiceModel();
        departmentServiceModel.setDepartmentName(DepartmentName.DEPARTMENT_I);
        userServiceModel.setDepartment(departmentServiceModel);

        userServiceModel.setRole("ROLE_USER");

        return userServiceModel;
    }

    private UserServiceModel createExistingUserServiceModel() {

        UserServiceModel userServiceModel = this.createUserServiceModel();
        userServiceModel.setId(1L);

        return userServiceModel;
    }

    private DepartmentEntity createExistingDepartmentEntity(){

        DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(1L);
        departmentEntity.setDepartmentName(DepartmentName.DEPARTMENT_I);

        return departmentEntity;
    }
}
