package warehouse.users.model;

import org.hibernate.validator.constraints.Length;
import warehouse.departments.model.DepartmentServiceModel;
import warehouse.users.validation.ValidDepartment;
import warehouse.users.validation.ValidRole;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.constraints.*;

public class UserServiceModel {

    private Long id;
    private String username;
    private String password;
    private String email;
    private DepartmentServiceModel department;
    private String role;
    private boolean isEnabled;


    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotBlank
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotBlank
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters long")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NotBlank
    @Email(message = "Enter valid email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DepartmentServiceModel getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentServiceModel department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public UserServiceModel setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }
}
