package warehouse.departments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.Assert;
import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;
import warehouse.departments.repository.DepartmentRepository;
import warehouse.departments.service.DepartmentService;
import warehouse.departments.service.impl.DepartmentServiceImpl;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepartmentsServiceUnitTests {

    private DepartmentService departmentServiceToTest;
    private DepartmentEntity departmentEntity;

    @Mock
    DepartmentRepository mockDepartmentRepository;

    @BeforeEach
    public void setUp(){
        this.departmentServiceToTest = new DepartmentServiceImpl(mockDepartmentRepository);
        this.departmentEntity = this.createExistingDepartmentEntity();
    }

   @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByName(){

        when(mockDepartmentRepository.findByDepartmentName(any(DepartmentName.class))).thenReturn(Optional.of(departmentEntity));

        DepartmentEntity existingDepartmentEntity = this.departmentServiceToTest.findByName(DepartmentName.DEPARTMENT_I);

       Assertions.assertEquals(1L, existingDepartmentEntity.getId());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByNameThrowsEntityNotFoundException(){

        when(mockDepartmentRepository.findByDepartmentName(any(DepartmentName.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.departmentServiceToTest.findByName(DepartmentName.DEPARTMENT_I));
    }

    private DepartmentEntity createExistingDepartmentEntity() {

        DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setDepartmentName(DepartmentName.DEPARTMENT_I);
        departmentEntity.setId(1L);

        return departmentEntity;
    }
}
