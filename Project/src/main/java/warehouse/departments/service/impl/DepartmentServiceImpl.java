package warehouse.departments.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;
import warehouse.departments.repository.DepartmentRepository;
import warehouse.departments.service.DepartmentService;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DepartmentEntity findByName(DepartmentName departmentName) {

        DepartmentEntity departmentEntity = this.departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new EntityNotFoundException("Not found department with name: " + departmentName.name()));

        return departmentEntity;
    }

    @Override
    public void initDepartments() {

        if (this.departmentRepository.count() == 0) {
            Arrays.stream(DepartmentName.values()).forEach(departmentName -> {
                DepartmentEntity departmentEntity = new DepartmentEntity();
                departmentEntity.setDepartmentName(departmentName);
                this.departmentRepository.saveAndFlush(departmentEntity);
            });
        }
    }
}
