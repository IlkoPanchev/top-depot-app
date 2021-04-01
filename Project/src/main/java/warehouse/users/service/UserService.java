package warehouse.users.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.users.model.UserEntity;
import warehouse.users.model.UserServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
@Validated
public interface UserService {

    Optional<UserEntity> findUserByUsername(String username);

    UserServiceModel getUserByUserName(String username);

    @Validated(OnCreate.class)
    UserServiceModel register(@Valid UserServiceModel model);

    List<String> findAllUsernames();

    void addRole(Long userId, String role);

    void removeRole(Long userId, String role);

    boolean userExists(String username);

    List<UserServiceModel> findAll();

    Page<UserServiceModel> findAllPageable(Pageable pageable);

//    List<UserServiceModel> filter(String keyword);

    UserServiceModel findUserById(Long id);

    @Validated(OnUpdate.class)
    UserServiceModel edit(@Valid UserServiceModel model);

//    UserServiceModel delete(Long id);

    Set<String> getRolesByUserId(Long id);

    Page<UserServiceModel> search(String keyword, Pageable pageable);

    UserServiceModel block(Long id);

    UserServiceModel unblock(Long id);

    void initUsers();
}
