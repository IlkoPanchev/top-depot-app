package warehouse.categories.model;

import org.hibernate.validator.constraints.Length;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

public class CategoryAddBindingModel {
    private Long id;
    private String name;
    private String description;


    @Null(groups = OnCreate.class)
    @NotNull(groups = OnUpdate.class)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotBlank
    @Size(min = 3, message = "Name must be at least 3 characters long")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
