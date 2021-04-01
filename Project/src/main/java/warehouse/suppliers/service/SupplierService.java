package warehouse.suppliers.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.model.SupplierTurnoverViewModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
@Validated
public interface SupplierService {

    @Validated(OnCreate.class)
    SupplierServiceModel add(@Valid SupplierServiceModel supplierServiceModel);

    List<SupplierServiceModel> findAll();

    Page<SupplierServiceModel> findAllPageable(Pageable pageable);

//    List<SupplierServiceModel> updateAll();

    SupplierEntity findByName(String name);

    Page<SupplierServiceModel> search(String keyword, Pageable pageable);

    SupplierServiceModel findById(Long id);

//    SupplierServiceModel delete(Long id);

    boolean supplierExists(String name);

    @Validated(OnUpdate.class)
    SupplierServiceModel edit(@Valid SupplierServiceModel supplierServiceModel);

    SupplierServiceModel block(Long id);

    SupplierServiceModel unblock(Long id);

    HashMap<Integer, String> getTopSuppliersNamesMap(String fromDate, String toDate);

    HashMap<Integer, BigDecimal> getTopSuppliersTurnoverMap(String fromDate, String toDate);

    List<SupplierTurnoverViewModel> getSupplierTurnover(String keyword, String fromDate, String toDate);

    void initSuppliers();

    SupplierEntity getById(long id);

    List<String> getAllSupplierNames();
}
