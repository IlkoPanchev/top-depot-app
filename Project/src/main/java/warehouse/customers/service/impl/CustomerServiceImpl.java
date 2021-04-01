package warehouse.customers.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.service.AddressService;
import warehouse.customers.model.CustomerAddBindingModel;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.model.CustomerTurnoverViewModel;
import warehouse.customers.repository.CustomerRepository;
import warehouse.customers.service.CustomerService;
import warehouse.orders.service.OrderService;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    private final TimeBordersConvertor timeBordersConvertor;
    private final OrderService orderService;
    private final AddressService addressService;
    private final ValidationUtil validationUtil;


    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               ModelMapper modelMapper,
                               TimeBordersConvertor timeBordersConvertor,
                               @Lazy OrderService orderService,
                               AddressService addressService,
                               ValidationUtil validationUtil) {
        this.customerRepository = customerRepository;
        this.modelMapper = modelMapper;
        this.timeBordersConvertor = timeBordersConvertor;
        this.orderService = orderService;
        this.addressService = addressService;
        this.validationUtil = validationUtil;
    }


    @Override
    public boolean customerExists(String companyName) {

        Optional<CustomerEntity> existingCustomer = this.customerRepository
                .findByCompanyName(companyName);

        return existingCustomer.isPresent();
    }

    @Override
    public CustomerServiceModel add(CustomerServiceModel customerServiceModel) {

        if (this.validationUtil.isValid(customerServiceModel)){

            CustomerEntity customerEntity = this.modelMapper.map(customerServiceModel, CustomerEntity.class);
            AddressEntity addressEntity = this.modelMapper.map(customerServiceModel.getAddress(), AddressEntity.class);

            customerEntity.setAddressEntity(addressEntity);
            customerEntity = this.customerRepository.saveAndFlush(customerEntity);
            customerServiceModel =  this.modelMapper.map(customerEntity, CustomerServiceModel.class);
        }
        else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(customerServiceModel));
        }

        return customerServiceModel;
    }


    @Override
    public List<CustomerServiceModel> findAll() {

        List<CustomerEntity> customerEntityList = this.customerRepository.findAll();
        List<CustomerServiceModel> customerServiceList = customerEntityList.stream()
                .map(customerEntity -> this.modelMapper.map(customerEntity, CustomerServiceModel.class)).collect(Collectors.toList());

        return customerServiceList;
    }

    @Override
    public Page<CustomerServiceModel> findAllPageable(Pageable pageable) {

        Page<CustomerEntity> customerEntities = this.customerRepository.findAll(pageable);

        List<CustomerServiceModel> customerServiceModels = customerEntities.stream()
                .map(customerEntity -> this.modelMapper.map(customerEntity, CustomerServiceModel.class)).collect(Collectors.toList());

        Page<CustomerServiceModel> customerServiceModelPage = new PageImpl<>(customerServiceModels, pageable, customerEntities.getTotalElements());

        return customerServiceModelPage;
    }

    @Override
    public Page<CustomerServiceModel> findAllPageableUnblocked(Pageable pageable) {

        Page<CustomerEntity> customerEntities = this.customerRepository.findAllByBlockedFalse(pageable);

        List<CustomerServiceModel> customerServiceModels = customerEntities.stream()
                .map(customerEntity -> this.modelMapper.map(customerEntity, CustomerServiceModel.class)).collect(Collectors.toList());

        Page<CustomerServiceModel> customerServiceModelPage = new PageImpl<>(customerServiceModels, pageable, customerEntities.getTotalElements());

        return customerServiceModelPage;
    }

    @Override
    public CustomerServiceModel findById(Long id) {

        CustomerEntity customerEntity = this.customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found customer with id: " + id));

        CustomerServiceModel customerServiceModel = this.modelMapper.map(customerEntity, CustomerServiceModel.class);

        return customerServiceModel;
    }


    @Override
    public Page<CustomerServiceModel> search(String keyword, Pageable pageable) {

        Page<CustomerEntity> customerEntities = this.customerRepository.search(keyword, pageable);

        List<CustomerServiceModel> customerServiceModels = customerEntities.stream()
                .map(customerEntity -> this.modelMapper.map(customerEntity, CustomerServiceModel.class)).collect(Collectors.toList());

        Page<CustomerServiceModel> customerServiceModelPage = new PageImpl<>(customerServiceModels, pageable, customerEntities.getTotalElements());

        return customerServiceModelPage;
    }

    @Override
    public Page<CustomerServiceModel> searchUnblocked(String keyword, Pageable pageable) {

        Page<CustomerEntity> customerEntities = this.customerRepository.searchUnblocked(keyword, pageable);

        List<CustomerServiceModel> customerServiceModels = customerEntities.stream()
                .map(customerEntity -> this.modelMapper.map(customerEntity, CustomerServiceModel.class)).collect(Collectors.toList());

        Page<CustomerServiceModel> customerServiceModelPage = new PageImpl<>(customerServiceModels, pageable, customerEntities.getTotalElements());

        return customerServiceModelPage;
    }

//    @Override
//    public Page<CustomerViewBindingModel> getPage(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize) {
//
//        String option = sortOptions.get(sortOption);
//
//        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
//                Sort.by(option).descending();
//        Pageable pageable = PageRequest.of(page, pageSize, sort);
//
//        Page<CustomerServiceModel> customerServiceModelPage;
//
//        if (!keyword.equals("null")) {
//            customerServiceModelPage = this.search(keyword, pageable);
//        } else {
//            customerServiceModelPage = this.findAllPageable(pageable);
//        }
//
//        List<CustomerViewBindingModel> customerViewBindingModels = customerServiceModelPage
//                .stream()
//                .map(customerServiceModel -> this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class))
//                .collect(Collectors.toList());
//
//        Page<CustomerViewBindingModel> pageToReturn = new PageImpl<>(customerViewBindingModels, pageable, customerServiceModelPage.getTotalElements());
//
//        return pageToReturn;
//    }

    // TODO decide about getPage()

    @Override
    public CustomerServiceModel edit(CustomerServiceModel customerServiceModel) {

        if(this.validationUtil.isValid(customerServiceModel)){

            long id = customerServiceModel.getId();
            CustomerEntity customerEntity = this.customerRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Not found customer with id: " + id));

            long addressId = customerServiceModel.getAddress().getId();
            if(!this.addressService.addressExists(addressId)){
                throw new EntityNotFoundException("Not found address with id: " + addressId);
            }

            boolean currentStatus = customerEntity.isBlocked();
            customerEntity = this.modelMapper.map(customerServiceModel, CustomerEntity.class);
            customerEntity.setBlocked(currentStatus);
            AddressEntity addressEntity = this.modelMapper.map(customerServiceModel.getAddress(), AddressEntity.class);
            customerEntity.setAddressEntity(addressEntity);
            customerEntity = this.customerRepository.saveAndFlush(customerEntity);
            customerServiceModel = this.modelMapper.map(customerEntity, CustomerServiceModel.class);

        }
        else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(customerServiceModel));

        }
        return customerServiceModel;
    }

    @Override
    public CustomerServiceModel block(Long id) {

        CustomerEntity customerEntity = this.customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found customer with id: " + id));

        customerEntity.setBlocked(true);
        this.customerRepository.saveAndFlush(customerEntity);

        return this.modelMapper.map(customerEntity, CustomerServiceModel.class);
    }

    @Override
    public CustomerServiceModel unblock(Long id) {

        CustomerEntity customerEntity = this.customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found customer with id: " + id));

        customerEntity.setBlocked(false);
        this.customerRepository.saveAndFlush(customerEntity);

        return this.modelMapper.map(customerEntity, CustomerServiceModel.class);
    }

    @Override
    public List<CustomerTurnoverViewModel> getCustomerTurnover(String keyword, String fromDate, String toDate) {

        List<CustomerTurnoverViewModel> customers = new ArrayList<>();
        LocalDateTime[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.customerRepository.findCustomerTurnover(timeBorders[0], timeBorders[1], keyword);

        for (Object[] objects : result) {
            CustomerTurnoverViewModel customer = new CustomerTurnoverViewModel();
            customer.setCompanyName(String.valueOf(objects[0]));
            customer.setPersonName(String.valueOf(objects[1]));
            customer.setTurnover(new BigDecimal(String.valueOf(objects[2])));
            customer.setOrdersCount(Integer.parseInt(String.valueOf(objects[3])));
            customer.setOrderedItems(Integer.parseInt(String.valueOf(objects[4])));
            customers.add(customer);
        }
        return customers;
    }

    @Override
    @Transactional
    public void initCustomers() {

        if (this.customerRepository.count() == 0) {
            for (int i = 1; i < INIT_COUNT; i++) {

                CustomerEntity customerEntity = new CustomerEntity();
                customerEntity.setCompanyName(String.format("Company_Name_%d", i));
                customerEntity.setPersonName(String.format("Person_Name_%d", i));
                customerEntity.setEmail(String.format("cust_email_%d_@mail.bg", i));
                customerEntity.setAddressEntity(this.addressService.getById(i));

                this.customerRepository.saveAndFlush(customerEntity);

            }
        }
    }

    @Override
    public CustomerEntity getById(long id) {

        CustomerEntity customerEntity = this.customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found customer with id: " + id));

        return customerEntity;
    }
}
