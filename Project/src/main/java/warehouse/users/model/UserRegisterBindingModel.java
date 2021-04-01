package warehouse.users.model;

import warehouse.departments.model.DepartmentName;
import warehouse.users.validation.ValidDepartment;
import warehouse.users.validation.ValidRole;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.constraints.*;


public class UserRegisterBindingModel {


    private Long id;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private DepartmentName department;
    private String role;


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
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @NotBlank
    @Email(message = "Enter valid email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    @ValidDepartment(message = "Select valid department")
    public DepartmentName getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentName department) {
        this.department = department;
    }



    @ValidRole(message = "Select valid role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
