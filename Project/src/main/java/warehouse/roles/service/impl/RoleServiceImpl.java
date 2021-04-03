package warehouse.roles.service.impl;

import org.springframework.stereotype.Service;
import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;
import warehouse.roles.repository.RoleRepository;
import warehouse.roles.service.RoleService;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void initRoles() {

        if (this.roleRepository.count() == 0) {

            RoleEntity admin = new RoleEntity();
            admin.setRole(RoleName.ROLE_ADMIN);

            RoleEntity manager = new RoleEntity();
            manager.setRole(RoleName.ROLE_MANAGER);

            RoleEntity user = new RoleEntity();
            user.setRole(RoleName.ROLE_USER);

          this.roleRepository.saveAll(List.of(admin, manager, user));
        }

    }

    @Override
    public RoleEntity getByName(RoleName name) {

        RoleEntity roleEntity = this.roleRepository.findByRole(name).orElseThrow(() -> new EntityNotFoundException("Not found role with name :" + name.name()));
        return roleEntity;
    }


}
