package warehouse.items.validation;

import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.service.SupplierService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierValidator implements ConstraintValidator<ValidSupplier, String> {


    private final SupplierService supplierService;

    public SupplierValidator(SupplierService supplierService) {
        this.supplierService = supplierService;
    }


    @Override
    public boolean isValid(String supplier, ConstraintValidatorContext constraintValidatorContext) {

        List<String> suppliers = this.supplierService.findAll().stream().map(SupplierServiceModel::getName).collect(Collectors.toList());

        return suppliers.contains(supplier);
    }
}
