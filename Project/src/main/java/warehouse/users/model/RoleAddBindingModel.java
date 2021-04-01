package warehouse.users.model;


import warehouse.users.validation.ValidRole;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;


public class RoleAddBindingModel {
    private Long userId;
    private String role;

    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @ValidRole(message = "Select valid role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
