package warehouse.users.model;

import warehouse.departments.model.DepartmentName;

public class UserViewBindingModel {



    private Long id;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private DepartmentName department;
    private String role;
    private boolean isEnabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DepartmentName getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentName department) {
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

    public UserViewBindingModel setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }
}
