package warehouse.roles.model;

import warehouse.base.BaseEntity;
import warehouse.roles.model.RoleName;

import javax.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {


    private RoleName role;


    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }

}
