package warehouse.departments.model;

import warehouse.base.BaseEntity;
import warehouse.users.model.UserEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity {

    private DepartmentName departmentName;
    private Set<UserEntity> users;


    @Column(name = "department_name", nullable = false)
    @Enumerated(EnumType.STRING)
    public DepartmentName getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(DepartmentName departmentName) {
        this.departmentName = departmentName;
    }

    @OneToMany(mappedBy = "department", fetch = FetchType.EAGER)
    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }
}
