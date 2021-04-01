package warehouse.customers.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import warehouse.customers.model.*;
import warehouse.items.model.ItemViewBindingModel;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Validated
public interface CustomerService {

    boolean customerExists(String companyName);

    @Validated(OnCreate.class)
    CustomerServiceModel add(@Valid CustomerServiceModel customerServiceModel);

    List<CustomerServiceModel> findAll();

    Page<CustomerServiceModel> findAllPageable(Pageable pageable);

    Page<CustomerServiceModel> findAllPageableUnblocked(Pageable pageable);

    CustomerServiceModel findById(Long id);

//    CustomerServiceModel delete(Long id);

    Page<CustomerServiceModel> search(String keyword, Pageable pageable);

    Page<CustomerServiceModel> searchUnblocked(String keyword, Pageable pageable);

//    Page<CustomerViewBindingModel> getPage(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize);

    @Validated(OnUpdate.class)
    CustomerServiceModel edit(@Valid CustomerServiceModel customerServiceModel);

    CustomerServiceModel block(Long id);

    CustomerServiceModel unblock(Long id);

    List<CustomerTurnoverViewModel> getCustomerTurnover(String keyword, String fromDate, String toDate);

    void initCustomers();

    CustomerEntity getById(long id);
}
