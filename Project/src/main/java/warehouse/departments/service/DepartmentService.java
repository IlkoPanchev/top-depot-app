package warehouse.departments.service;

import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;

public interface DepartmentService {

    DepartmentEntity findByName(DepartmentName departmentName);

    void initDepartments();
}
