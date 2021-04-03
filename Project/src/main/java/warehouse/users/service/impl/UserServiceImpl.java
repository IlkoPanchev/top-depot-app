package warehouse.users.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;
import warehouse.departments.service.DepartmentService;
import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;
import warehouse.roles.service.RoleService;
import warehouse.users.model.*;
import warehouse.users.repository.UserRepository;
import warehouse.users.service.UserService;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;
    private final ValidationUtil validationUtil;
    private final RoleService roleService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, DepartmentService departmentService, PasswordEncoder passwordEncoder, ValidationUtil validationUtil, RoleService roleService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.validationUtil = validationUtil;
        this.roleService = roleService;
    }

    @Override
    public Optional<UserEntity> findUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    @Override
    public UserServiceModel getUserByUserName(String username) {

        UserEntity user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with username: " + username));

        UserServiceModel userServiceModel = this.modelMapper.map(user, UserServiceModel.class);

        return userServiceModel;
    }

    @Override
    public UserServiceModel register(UserServiceModel userServiceModel) {

        if (this.validationUtil.isValid(userServiceModel)){

            this.validateDepartment(userServiceModel);

            this.validateRole(userServiceModel);

            UserEntity userEntity = this.modelMapper.map(userServiceModel, UserEntity.class);
            userEntity.setPassword(this.passwordEncoder.encode(userServiceModel.getPassword()));
            userEntity.setDepartment(this.departmentService.findByName(userServiceModel.getDepartment().getDepartmentName()));
            userEntity.setEnabled(true);

            String role = userServiceModel.getRole();
            this.setRoleToUser(userEntity, role);

            userServiceModel = this.modelMapper.map(this.userRepository.saveAndFlush(userEntity), UserServiceModel.class);

        }
        else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(userServiceModel));

        }

        return userServiceModel;
    }

    @Override
    public void addRole(Long userId, String role) {

        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + userId));

        RoleEntity roleEntity = userEntity.getRoles()
                .stream().filter(r -> r.getRole().name().equals(role))
                .findFirst().orElse(null);

        if (roleEntity == null) {
            if (role.equals("ROLE_ADMIN")) {
                userEntity.addRole(this.getRole("ROLE_ADMIN"));
            } else if (role.equals("ROLE_MANAGER")) {
                userEntity.addRole(this.getRole("ROLE_MANAGER"));
            } else if (role.equals("ROLE_USER")) {
                userEntity.addRole(this.getRole("ROLE_USER"));
            }
        }
        this.userRepository.saveAndFlush(userEntity);
    }

    @Override
    public void removeRole(Long userId, String role) {

        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + userId));

        RoleEntity roleEntity = userEntity.getRoles()
                .stream().filter(r -> r.getRole().name().equals(role))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Not found role : " + role));

        userEntity.removeRole(roleEntity);

        this.userRepository.saveAndFlush(userEntity);
    }

    @Override
    public boolean userExists(String username) {

        Optional<UserEntity> existingUser = this.userRepository.findByUsername(username);

        return existingUser.isPresent();
    }

    @Override
    public Page<UserServiceModel> findAllPageable(Pageable pageable) {

        Page<UserEntity> userEntities = this.userRepository.findAll(pageable);

        List<UserServiceModel> userServiceModels = userEntities.stream()
                .map(userEntity -> this.modelMapper.map(userEntity, UserServiceModel.class)).collect(Collectors.toList());

        Page<UserServiceModel> userServiceModelPage = new PageImpl<>(userServiceModels, pageable, userEntities.getTotalElements());

        return userServiceModelPage;
    }

    @Override
    public UserServiceModel findUserById(Long id) {

        UserEntity userEntity = this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + id));

        UserServiceModel userServiceModel = this.modelMapper.map(userEntity, UserServiceModel.class);

        return userServiceModel;
    }

    @Override
    public UserServiceModel edit(UserServiceModel userServiceModel) {

        if (this.validationUtil.isValid(userServiceModel)){

            this.validateDepartment(userServiceModel);

            this.validateRole(userServiceModel);

            long id = userServiceModel.getId();
            this.userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + id));

            UserEntity userEntity;
            userEntity = this.modelMapper.map(userServiceModel, UserEntity.class);
            userEntity.setPassword(this.passwordEncoder.encode(userServiceModel.getPassword()));
            userEntity.setDepartment(this.departmentService.findByName(userServiceModel.getDepartment().getDepartmentName()));
            Set<RoleEntity> roles = this.userRepository.findById(userServiceModel.getId()).orElse(null).getRoles();
            userEntity.setRoles(roles);
            boolean isEnabled = this.userRepository.findById(userServiceModel.getId()).orElse(null).isEnabled();
            userEntity.setEnabled(isEnabled);
            userEntity = this.userRepository.saveAndFlush(userEntity);
            userServiceModel = this.modelMapper.map(userEntity, UserServiceModel.class);

        }
        else {
            throw new ConstraintViolationException(this.validationUtil.getViolations(userServiceModel));
        }
        return userServiceModel;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserServiceModel block(Long id) {

        UserEntity userEntity = this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + id));

        userEntity.setEnabled(false);
        this.userRepository.saveAndFlush(userEntity);

        return this.modelMapper.map(userEntity, UserServiceModel.class);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserServiceModel unblock(Long id) {

        UserEntity userEntity = this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + id));

        userEntity.setEnabled(true);
        this.userRepository.saveAndFlush(userEntity);

        return this.modelMapper.map(userEntity, UserServiceModel.class);
    }

    @Override
    public Set<String> getRolesByUserId(Long id) {

        UserEntity userEntity = this.userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id: " + id));

        Set<String> userRoles = new TreeSet<>();
        userEntity.getRoles().forEach(roleEntity -> userRoles.add(roleEntity.getRole().name()));
        return userRoles;
    }

    @Override
    public Page<UserServiceModel> search(String keyword, Pageable pageable) {

        Page<UserEntity> userEntities = this.userRepository.search(keyword, pageable);

        List<UserServiceModel> userServiceModels = userEntities
                .stream()
                .map(userEntity -> this.modelMapper.map(userEntity, UserServiceModel.class))
                .collect(Collectors.toList());

        Page<UserServiceModel> userServiceModelPage = new PageImpl<>(userServiceModels, pageable, userEntities.getTotalElements());

        return userServiceModelPage;

    }

    @Override
    public void initUsers() {

        if (userRepository.count() == 0) {

            createUser("ROLE_ADMIN", "DEPARTMENT_I", "admin", "admin@mail.bg", "aaa");
            createUser("ROLE_MANAGER", "DEPARTMENT_II","manager_1", "manager_1@mail.bg", "mmm");
            createUser("ROLE_MANAGER", "DEPARTMENT_III","manager_2", "manager_2@mail.bg", "mmmm");
            createUser("ROLE_USER", "DEPARTMENT_II","user_1", "user_1@mail.bg", "uuu");
            createUser("ROLE_USER", "DEPARTMENT_III","user_2", "user_2@mail.bg", "uuuu");
            createUser("ROLE_USER", "DEPARTMENT_III","user_3", "user_3@mail.bg", "uuuuu");

        }
    }


    private void validateRole(UserServiceModel userServiceModel) {

        List<String> roles = Arrays.stream(RoleName.values()).map(RoleName::name).collect(Collectors.toList());


        boolean isRoleValid = roles.contains(userServiceModel.getRole());
        if (!isRoleValid) {
            throw new NoSuchElementException(String.format("Role '%s' does not exist!",
                    userServiceModel.getRole()));
        }
    }

    private void validateDepartment(UserServiceModel userServiceModel) {

        List<String> departmentNames = Arrays.stream(DepartmentName.values())
                .map(DepartmentName::name).collect(Collectors.toList());

        boolean isDepartmentValid = departmentNames.contains(userServiceModel.getDepartment().getDepartmentName().name());
        if (!isDepartmentValid) {
            throw new NoSuchElementException(String.format("Department '%s' does not exist!",
                    userServiceModel.getDepartment().getDepartmentName().name()));
        }


    }


    private void createUser(String role, String department, String username, String email, String password) {

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        DepartmentEntity departmentEntity = this.departmentService
                .findByName(DepartmentName.valueOf(department));
        user.setDepartment(departmentEntity);

        this.setRoleToUser(user, role);

        userRepository.save(user);
    }

    private void setRoleToUser(UserEntity user, String role) {

        if (role.equals("ROLE_ADMIN")) {
            user.addRole(this.getRole("ROLE_ADMIN"));
            user.addRole(this.getRole("ROLE_MANAGER"));
            user.addRole(this.getRole("ROLE_USER"));
        } else if (role.equals("ROLE_MANAGER")) {
            user.addRole(this.getRole("ROLE_MANAGER"));
            user.addRole(this.getRole("ROLE_USER"));
        } else if (role.equals("ROLE_USER")) {
            user.addRole(this.getRole("ROLE_USER"));
        }
    }


    private RoleEntity getRole(String role) {

        RoleEntity roleEntity = this.roleService.getByName(RoleName.valueOf(role));
        return roleEntity;
    }

}
