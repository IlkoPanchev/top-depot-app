package warehouse.suppliers.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.service.AddressService;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.model.SupplierTurnoverViewModel;
import warehouse.suppliers.repository.SupplierRepository;
import warehouse.suppliers.service.SupplierService;
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
import java.util.*;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;
    private final OrderService orderService;
    private final TimeBordersConvertor timeBordersConvertor;
    private final AddressService addressService;
    private final ValidationUtil validationUtil;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository, ModelMapper modelMapper, OrderService orderService, TimeBordersConvertor timeBordersConvertor, AddressService addressService, ValidationUtil validationUtil) {
        this.supplierRepository = supplierRepository;
        this.modelMapper = modelMapper;
        this.orderService = orderService;
        this.timeBordersConvertor = timeBordersConvertor;
        this.addressService = addressService;
        this.validationUtil = validationUtil;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierServiceModel add(SupplierServiceModel supplierServiceModel) {

        if (this.validationUtil.isValid(supplierServiceModel)){

            SupplierEntity supplierEntity = this.modelMapper.map(supplierServiceModel, SupplierEntity.class);
            AddressEntity addressEntity = this.modelMapper.map(supplierServiceModel.getAddress(), AddressEntity.class);
            supplierEntity.setAddressEntity(addressEntity);
            supplierEntity = this.supplierRepository.saveAndFlush(supplierEntity);
            supplierServiceModel = this.modelMapper.map(supplierEntity, SupplierServiceModel.class);

        }
        else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(supplierServiceModel));
        }

        return supplierServiceModel;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SupplierServiceModel> findAll() {
        return this.supplierRepository.findAll()
                .stream()
                .map(supplierEntity -> this.modelMapper.map(supplierEntity, SupplierServiceModel.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Page<SupplierServiceModel> findAllPageable(Pageable pageable) {


        Page<SupplierEntity> supplierEntities = this.supplierRepository.findAll(pageable);

        List<SupplierServiceModel> supplierServiceModels = supplierEntities.stream()
                .map(supplierEntity -> this.modelMapper.map(supplierEntity, SupplierServiceModel.class)).collect(Collectors.toList());

        Page<SupplierServiceModel> orderViewServiceModelsPage = new PageImpl<>(supplierServiceModels, pageable, supplierEntities.getTotalElements());

        return orderViewServiceModelsPage;
    }


    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")

    public SupplierEntity findByName(String name) {

        SupplierEntity supplierEntity = this.supplierRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Not found supplier with name: " + name));

        return supplierEntity;
    }

    @Override
    public Page<SupplierServiceModel> search(String keyword, Pageable pageable) {


        Page<SupplierEntity> supplierEntities = this.supplierRepository.search(keyword, pageable);

        List<SupplierServiceModel> supplierServiceModels = supplierEntities.stream()
                .map(supplierEntity -> this.modelMapper.map(supplierEntity, SupplierServiceModel.class)).collect(Collectors.toList());

        Page<SupplierServiceModel> orderViewServiceModelsPage = new PageImpl<>(supplierServiceModels, pageable, supplierEntities.getTotalElements());

        return orderViewServiceModelsPage;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierServiceModel findById(Long id) {

        SupplierEntity supplierEntity = this.supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found supplier with id: " + id));

        return this.modelMapper.map(supplierEntity, SupplierServiceModel.class);
    }


    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public boolean supplierExists(String name) {

        Optional<SupplierEntity> supplierEntity = this.supplierRepository.findByName(name);

        return supplierEntity.isPresent();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierServiceModel edit(SupplierServiceModel supplierServiceModel) {


        if (this.validationUtil.isValid(supplierServiceModel)){

            long supplierId = supplierServiceModel.getId();
            SupplierEntity supplierEntity = this.supplierRepository
                    .findById(supplierId).orElseThrow(() -> new EntityNotFoundException("Not found supplier with id: " + supplierId));

            long addressId = supplierServiceModel.getAddress().getId();
            if(!this.addressService.addressExists(addressId)){
                throw new EntityNotFoundException("Not found address with id: " + addressId);
            }

            boolean currentStatus = supplierEntity.isBlocked();
            supplierEntity = this.modelMapper.map(supplierServiceModel, SupplierEntity.class);
            supplierEntity.setBlocked(currentStatus);
            AddressEntity addressEntity = this.modelMapper.map(supplierServiceModel.getAddress(), AddressEntity.class);
            supplierEntity.setAddressEntity(addressEntity);
            supplierEntity = this.supplierRepository.saveAndFlush(supplierEntity);
            supplierServiceModel = this.modelMapper.map(supplierEntity, SupplierServiceModel.class);

        }
        else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(supplierServiceModel));

        }
        return supplierServiceModel;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierServiceModel block(Long id) {

        SupplierEntity supplierEntity = this.supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found supplier with id: " + id));

        supplierEntity.setBlocked(true);
        this.supplierRepository.saveAndFlush(supplierEntity);

        return this.modelMapper.map(supplierEntity, SupplierServiceModel.class);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public SupplierServiceModel unblock(Long id) {

        SupplierEntity supplierEntity = this.supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found supplier with id: " + id));

        supplierEntity.setBlocked(false);
        this.supplierRepository.saveAndFlush(supplierEntity);

        return this.modelMapper.map(supplierEntity, SupplierServiceModel.class);
    }


    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public HashMap<Integer, String> getTopSuppliersNamesMap(String fromDate, String toDate) {

        HashMap<Integer, String> suppliersNamesMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());
        String[] timeBordersPieChart = this.timeBordersConvertor
                .getTimeBordersAsString(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.supplierRepository.findTopSuppliers(timeBorders[0], timeBorders[1]);

        return this.timeBordersConvertor.getBordersAndNamesMap(suppliersNamesMap, timeBordersPieChart, result);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public HashMap<Integer, BigDecimal> getTopSuppliersTurnoverMap(String fromDate, String toDate) {

        HashMap<Integer, BigDecimal> suppliersTurnoverMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.supplierRepository.findTopSuppliers(timeBorders[0], timeBorders[1]);

        int key = 2;

        for (Object[] objects : result) {
            suppliersTurnoverMap.put(++key, new BigDecimal(String.valueOf(objects[1])));
        }

        return suppliersTurnoverMap;
    }



    @Override
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SupplierTurnoverViewModel> getSupplierTurnover(String keyword, String fromDate, String toDate) {

        List<SupplierTurnoverViewModel> suppliers = new ArrayList<>();
        LocalDateTime[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.supplierRepository.findSupplierTurnover(timeBorders[0], timeBorders[1], keyword);

        for (Object[] objects : result) {
            SupplierTurnoverViewModel supplier = new SupplierTurnoverViewModel();
            supplier.setName(String.valueOf(objects[0]));
            supplier.setTurnover(new BigDecimal(String.valueOf(objects[1])));
            supplier.setSoldItems(Integer.parseInt(String.valueOf(objects[2])));
            suppliers.add(supplier);
        }


        return suppliers;
    }

    @Transactional
    @Override
    public void initSuppliers() {

        if (this.supplierRepository.count() == 0) {

            for (int i = 1; i < INIT_COUNT; i++) {

                SupplierEntity supplierEntity = new SupplierEntity();
                supplierEntity.setName(String.format("Supplier_%d", i));
                supplierEntity.setEmail(String.format("supp_email_%d_@mail.bg", i));
                supplierEntity.setAddressEntity(this.addressService.getById(i));

                this.supplierRepository.saveAndFlush(supplierEntity);

            }
        }

    }

    @Override
    public SupplierEntity getById(long id) {

        SupplierEntity supplierEntity = this.supplierRepository.findById(id)

                .orElseThrow(() -> new EntityNotFoundException("Not found supplier with id: " + id));

        return supplierEntity;
    }

    @Override
    public List<String> getAllSupplierNames() {


        return this.supplierRepository.findAllSupplierNames();
    }

}
