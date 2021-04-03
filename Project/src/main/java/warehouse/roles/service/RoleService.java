package warehouse.roles.service;

import warehouse.roles.model.RoleEntity;
import warehouse.roles.model.RoleName;

public interface RoleService {

    void initRoles();


    RoleEntity getByName(RoleName name);
}
