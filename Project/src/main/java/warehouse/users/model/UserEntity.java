package warehouse.users.model;

import warehouse.base.BaseEntity;
import warehouse.departments.model.DepartmentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {


    private String username;
    private String password;
    private String email;
    private DepartmentEntity department;
    private boolean isEnabled = true;
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(name = "username", nullable = false, unique = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "password", nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Column(name = "isEnabled", nullable = false)
    public boolean isEnabled() {
        return isEnabled;
    }

    @Column(name = "email", nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
    public DepartmentEntity getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentEntity departmentEntity) {
        this.department = departmentEntity;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @OneToMany(mappedBy = "userEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }




    public void addRole(RoleEntity roleEntity) {
        if (this.roles.contains(roleEntity)) {
            return;
        }
        this.roles.add(roleEntity);
        roleEntity.setUserEntity(this);
    }

    public void removeRole(RoleEntity roleEntity) {
        if (!this.roles.contains(roleEntity)) {
            return;
        }
        this.roles.remove(roleEntity);
        roleEntity.setUserEntity(null);
    }
}
