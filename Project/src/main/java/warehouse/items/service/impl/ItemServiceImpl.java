package warehouse.items.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.repository.CategoryRepository;
import warehouse.categories.service.CategoryService;
import warehouse.cloudinary.CloudinaryService;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemAddServiceModel;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.items.repository.ItemRepository;
import warehouse.items.service.ItemService;
import warehouse.orderline.model.OrderLineAddServiceModel;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.model.OrderLineViewServiceModel;
import warehouse.orderline.service.OrderLineService;
import warehouse.orders.model.OrderAddServiceModel;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.repository.SupplierRepository;
import warehouse.suppliers.service.SupplierService;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class ItemServiceImpl implements ItemService {

    private final ModelMapper modelMapper;
    private final ItemRepository itemRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final OrderService orderService;
    private final TimeBordersConvertor timeBordersConvertor;
    private final ValidationUtil validationUtil;
    private final OrderLineService orderLineService;


    @Autowired
    public ItemServiceImpl(ModelMapper modelMapper,
                           ItemRepository itemRepository,
                           CloudinaryService cloudinaryService,
                           CategoryService categoryService,
                           SupplierService supplierService,
                           OrderService orderService,
                           TimeBordersConvertor timeBordersConvertor,
                           ValidationUtil validationUtil,
                           @Lazy OrderLineService orderLineService) {
        this.modelMapper = modelMapper;
        this.itemRepository = itemRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.orderService = orderService;
        this.timeBordersConvertor = timeBordersConvertor;
        this.validationUtil = validationUtil;
        this.orderLineService = orderLineService;
    }


    @Override
    public ItemAddServiceModel add(ItemAddServiceModel itemAddServiceModel) throws IOException {

        if (this.validationUtil.isValid(itemAddServiceModel)) {

            this.validateCategory(itemAddServiceModel);

            this.validateSupplier(itemAddServiceModel);

            ItemEntity itemEntity = this.modelMapper.map(itemAddServiceModel, ItemEntity.class);

            //TODO add item upload Multipart file

//            String img = "http://res.cloudinary.com/ipanchev/image/upload/v1616226988/id08tagytlyglzz84nyb.jpg";

            itemEntity.setImg(this.getCloudinaryLink(itemAddServiceModel));

            CategoryEntity categoryEntity = new CategoryEntity();
            CategoryEntity existingCategoryEntity = this.categoryService.findByName(itemAddServiceModel.getCategory());

            setCategoryFields(categoryEntity, existingCategoryEntity, itemEntity);

            itemEntity.setCategory(categoryEntity);

            SupplierEntity supplierEntity = new SupplierEntity();
            SupplierEntity existingSupplierEntity = this.supplierService.findByName(itemAddServiceModel.getSupplier());

            setSupplierFields(supplierEntity, existingSupplierEntity, itemEntity);

            itemEntity.setSupplier(supplierEntity);

            itemEntity = this.itemRepository.saveAndFlush(itemEntity);

            itemAddServiceModel = this.modelMapper.map(itemEntity, ItemAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(itemAddServiceModel));

        }

        return itemAddServiceModel;
    }


    @Override
    public Page<ItemViewServiceModel> findAllPageable(Pageable pageable) {

        Page<ItemEntity> itemEntities = this.itemRepository.findAll(pageable);

        List<ItemViewServiceModel> itemViewServiceModels = itemEntities
                .stream()
                .map(itemEntity -> this.modelMapper.map(itemEntity, ItemViewServiceModel.class))
                .collect(Collectors.toList());

        Page<ItemViewServiceModel> itemViewServiceModelPage = new PageImpl<>(itemViewServiceModels, pageable, itemEntities.getTotalElements());

        return itemViewServiceModelPage;
    }

    @Override
    public Page<ItemViewServiceModel> findAllPageableUnblocked(Pageable pageable) {

        Page<ItemEntity> itemEntities = this.itemRepository.findAllByBlockedFalse(pageable);

        List<ItemViewServiceModel> itemViewServiceModels = itemEntities
                .stream()
                .map(itemEntity -> this.modelMapper.map(itemEntity, ItemViewServiceModel.class))
                .collect(Collectors.toList());

        Page<ItemViewServiceModel> itemViewServiceModelPage = new PageImpl<>(itemViewServiceModels, pageable, itemEntities.getTotalElements());

        return itemViewServiceModelPage;
    }

    @Override
    public Page<ItemViewServiceModel> search(String keyword, Pageable pageable) {

        Page<ItemEntity> itemEntities = this.itemRepository.search(keyword, pageable);

        List<ItemViewServiceModel> itemViewServiceModels = itemEntities
                .stream()
                .map(itemEntity -> this.modelMapper.map(itemEntity, ItemViewServiceModel.class))
                .collect(Collectors.toList());

        Page<ItemViewServiceModel> itemViewServiceModelPage = new PageImpl<>(itemViewServiceModels, pageable, itemEntities.getTotalElements());

        return itemViewServiceModelPage;
    }

    @Override
    public Page<ItemViewServiceModel> searchUnblocked(String keyword, Pageable pageable) {

        Page<ItemEntity> itemEntities = this.itemRepository.searchUnblocked(keyword, pageable);

        List<ItemViewServiceModel> itemViewServiceModels = itemEntities
                .stream()
                .map(itemEntity -> this.modelMapper.map(itemEntity, ItemViewServiceModel.class))
                .collect(Collectors.toList());

        Page<ItemViewServiceModel> itemViewServiceModelPage = new PageImpl<>(itemViewServiceModels, pageable, itemEntities.getTotalElements());

        return itemViewServiceModelPage;
    }


    @Override
    public ItemViewServiceModel findById(Long id) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        ItemViewServiceModel itemViewServiceModel = this.modelMapper.map(itemEntity, ItemViewServiceModel.class);

        return itemViewServiceModel;
    }


    @Override
    public boolean itemExists(String name) {
        Optional<ItemEntity> itemEntity = this.itemRepository.findByName(name);
        return itemEntity.isPresent();
    }


    @Override
    public ItemViewServiceModel block(Long id) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        itemEntity.setBlocked(true);
        this.itemRepository.saveAndFlush(itemEntity);

        return this.modelMapper.map(itemEntity, ItemViewServiceModel.class);
    }

    @Override
    public ItemViewServiceModel unblock(Long id) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        itemEntity.setBlocked(false);
        this.itemRepository.saveAndFlush(itemEntity);

        return this.modelMapper.map(itemEntity, ItemViewServiceModel.class);
    }

    @Override
    public ItemAddServiceModel edit(ItemAddServiceModel itemAddServiceModel) throws IOException {

        if (this.validationUtil.isValid(itemAddServiceModel)) {

            this.validateCategory(itemAddServiceModel);

            this.validateSupplier(itemAddServiceModel);

            long id = itemAddServiceModel.getId();
            ItemEntity itemEntity = this.itemRepository
                    .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

            boolean currentStatus = itemEntity.isBlocked();
            itemEntity = this.modelMapper.map(itemAddServiceModel, ItemEntity.class);
            itemEntity.setBlocked(currentStatus);

            //TODO edit item upload Multipart file

//            String img = "http://res.cloudinary.com/ipanchev/image/upload/v1616226988/id08tagytlyglzz84nyb.jpg";

            itemEntity.setImg(this.getCloudinaryLink(itemAddServiceModel));

            CategoryEntity categoryEntity = new CategoryEntity();
            CategoryEntity existingCategoryEntity = this.categoryService.findByName(itemAddServiceModel.getCategory());

            setCategoryFields(categoryEntity, existingCategoryEntity, itemEntity);

            itemEntity.setCategory(categoryEntity);

            SupplierEntity supplierEntity = new SupplierEntity();
            SupplierEntity existingSupplierEntity = this.supplierService.findByName(itemAddServiceModel.getSupplier());

            setSupplierFields(supplierEntity, existingSupplierEntity, itemEntity);

            itemEntity.setSupplier(supplierEntity);

            itemEntity = this.itemRepository.saveAndFlush(itemEntity);

            itemAddServiceModel = this.modelMapper.map(itemEntity, ItemAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(itemAddServiceModel));

        }

        return itemAddServiceModel;
    }



    @Override
    public HashMap<Integer, String> getTopItemsNamesMap(String fromDate, String toDate) {

        HashMap<Integer, String> itemsNamesMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());
        String[] timeBordersPieChart = this.timeBordersConvertor
                .getTimeBordersAsString(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1], PageRequest.of(0, 5));

        return this.timeBordersConvertor.getBordersAndNamesMap(itemsNamesMap, timeBordersPieChart, result);
    }


    @Override
    public HashMap<Integer, Integer> getTopItemsQuantityMap(String fromDate, String toDate) {

        HashMap<Integer, Integer> itemsQuantityMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor.
                getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1], PageRequest.of(0, 5));

        int key = 2;
        for (Object[] objects : result) {
            itemsQuantityMap.put(++key, Integer.parseInt(String.valueOf(objects[1])));
        }

        return itemsQuantityMap;
    }

    @Override
    public HashMap<Integer, BigDecimal> getTopItemsTurnoverMap(String fromDate, String toDate) {

        HashMap<Integer, BigDecimal> itemsTurnoverMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor.
                getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1], PageRequest.of(0, 5));

        int key = 2;
        for (Object[] objects : result) {
            itemsTurnoverMap.put(++key, new BigDecimal(String.valueOf(objects[2])));
        }

        return itemsTurnoverMap;
    }

    @Override
    @Transactional
    public void initItems() {

        List<String> imgLinks = List.of("http://res.cloudinary.com/ipanchev/image/upload/v1617814213/qsixc8pkyitmed9x9i3s.jpg",
                "http://res.cloudinary.com/ipanchev/image/upload/v1616226988/id08tagytlyglzz84nyb.jpg",
                "http://res.cloudinary.com/ipanchev/image/upload/v1617814236/hw69wmhuzdfargmptsyn.jpg",
                "http://res.cloudinary.com/ipanchev/image/upload/v1617814248/eka24shqhsjjvawtknnd.jpg",
                "http://res.cloudinary.com/ipanchev/image/upload/v1617814263/txqxkhiybbfiav8laceb.jpg",
                "http://res.cloudinary.com/ipanchev/image/upload/v1617814276/axskgdmlhgt1gaumaxlb.jpg");

        if (this.itemRepository.count() == 0) {

            for (int i = 1; i < INIT_COUNT; i++) {

                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setName(String.format("Name_%d", i));
                itemEntity.setDescription(String.format("description_%d", i));
                itemEntity.setPrice((BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(i)));
                itemEntity.setStock(i * 10);
                itemEntity.setLocation(String.format("Location_%d", i));
                itemEntity.setCategory(this.categoryService.getById(i));
                itemEntity.setSupplier(this.supplierService.getById(i));
                itemEntity.setImg(imgLinks.get(i - 1));

                this.itemRepository.saveAndFlush(itemEntity);

            }
        }

    }

    @Override
    public ItemEntity getById(long id) {

        ItemEntity itemEntity = this.itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        return itemEntity;
    }

    @Override
    public boolean isStockEnough(Long id, int quantity) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        return itemEntity.getStock() >= quantity;
    }

    @Override
    public boolean isStockEnoughEditOrder(OrderViewServiceModel orderViewServiceModel,
                                          Long id, int newQuantity) {

        Set<OrderLineViewServiceModel> orderLineViewServiceModels = orderViewServiceModel.
                getOrderLineEntities();

        for (OrderLineViewServiceModel orderLineViewServiceModel : orderLineViewServiceModels) {

            if (orderLineViewServiceModel.getId().equals(id)){

                int oldQuantity = orderLineViewServiceModel.getQuantity();

                if ( oldQuantity < newQuantity){

                    int difference = newQuantity - oldQuantity;

                    ItemEntity itemEntity = this.itemRepository
                            .findById(orderLineViewServiceModel.getItem().getId()).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

                    return     itemEntity.getStock() >= difference;
                }
            }
        }

        return true;
    }


    @Override
    public void saveOrderUpdateStock(OrderAddServiceModel orderAddServiceModel) {

        Set<OrderLineAddServiceModel> orderLineAddServiceModels = orderAddServiceModel.getOrderLineEntities();

        for (OrderLineAddServiceModel orderLineAddServiceModel : orderLineAddServiceModels) {
            ItemEntity itemEntity = this.itemRepository
                    .findById(orderLineAddServiceModel
                            .getItem()
                            .getId())
                    .orElseThrow(() -> new EntityNotFoundException("Not found item with id: " +
                            orderLineAddServiceModel.getItem().getId()));
            itemEntity.setStock(itemEntity.getStock() - orderLineAddServiceModel.getQuantity());

            this.itemRepository.saveAndFlush(itemEntity);
        }

    }

    @Override
    public void editOrderUpdateStock(OrderViewServiceModel orderViewServiceModel, OrderAddServiceModel orderAddServiceModel) {

        Set<OrderLineViewServiceModel> orderLineViewServiceModels = orderViewServiceModel.
                getOrderLineEntities();

        Set<OrderLineAddServiceModel> orderLineAddServiceModels = orderAddServiceModel.
                getOrderLineEntities();

        Map<Long, OrderLineViewServiceModel> orderLineViewServiceModelHashMap = new HashMap<>();

        for (OrderLineViewServiceModel orderLineViewServiceModel : orderLineViewServiceModels) {

            orderLineViewServiceModelHashMap.put(orderLineViewServiceModel.getId(), orderLineViewServiceModel);

            Optional<OrderLineEntity> existingOrderLine = this.orderLineService.
                    findById(orderLineViewServiceModel.getId());

            if (existingOrderLine.isEmpty()){
                this.increaseItemStock(orderLineViewServiceModel.getItem().getId(), orderLineViewServiceModel.getQuantity());
            }
        }



        for (OrderLineAddServiceModel orderLineAddServiceModel : orderLineAddServiceModels) {

            if (orderLineViewServiceModelHashMap.containsKey(orderLineAddServiceModel.getId())){

                int oldQuantity = orderLineViewServiceModelHashMap.get(orderLineAddServiceModel.getId()).getQuantity();
                int newQuantity = orderLineAddServiceModel.getQuantity();

                int difference = 0;
                if (oldQuantity < newQuantity){
                    difference = newQuantity - oldQuantity;
                    this.decreaseItemStock(orderLineAddServiceModel.getItem().getId(), difference);
                }
                else if (oldQuantity > newQuantity){
                    difference = oldQuantity - newQuantity;
                    this.increaseItemStock(orderLineAddServiceModel.getItem().getId(), difference);
                }

            }
            else if (!orderLineViewServiceModelHashMap.containsKey(orderLineAddServiceModel.getId())){

                this.decreaseItemStock(orderLineAddServiceModel.getItem().getId(),
                        orderLineAddServiceModel.getQuantity());
            }
        }

    }

    @Override
    public void decreaseItemStock(Long id, int quantity) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        itemEntity.setStock(itemEntity.getStock() - quantity);
        this.itemRepository.saveAndFlush(itemEntity);
    }

    @Override
    public void increaseItemStock(Long id, int quantity) {

        ItemEntity itemEntity = this.itemRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found item with id: " + id));

        itemEntity.setStock(itemEntity.getStock() + quantity);
        this.itemRepository.saveAndFlush(itemEntity);
    }

    private String getCloudinaryLink(ItemAddServiceModel itemAddServiceModel) throws IOException {

        String img = null;

        MultipartFile multipartFile = itemAddServiceModel.getImg();

        if (multipartFile.getSize() == 0) {
            if (itemAddServiceModel.getId() != null) {
                ItemEntity existingEntity = this.itemRepository.findById(itemAddServiceModel.getId()).orElse(null);
                img = existingEntity.getImg();
            }
        } else {
            img = this.cloudinaryService.uploadImage(itemAddServiceModel.getImg());
        }

        return img;
    }

    private void setSupplierFields(SupplierEntity supplierEntity, SupplierEntity existingSupplierEntity, ItemEntity itemEntity) {
        supplierEntity.setId(existingSupplierEntity.getId());
        supplierEntity.setName(existingSupplierEntity.getName());
        supplierEntity.setEmail(existingSupplierEntity.getEmail());
        supplierEntity.addItem(itemEntity);
        supplierEntity.setAddressEntity(existingSupplierEntity.getAddressEntity());
    }

    private void setCategoryFields(CategoryEntity categoryEntity, CategoryEntity existingCategoryEntity, ItemEntity itemEntity) {
        categoryEntity.setId(existingCategoryEntity.getId());
        categoryEntity.setName(existingCategoryEntity.getName());
        categoryEntity.setDescription(existingCategoryEntity.getDescription());
        categoryEntity.addItem(itemEntity);
    }

    private void validateSupplier(ItemAddServiceModel itemAddServiceModel) {
        List<String> suppliers = this.supplierService.getAllSupplierNames();
        boolean isSupplierValid = suppliers.contains(itemAddServiceModel.getSupplier());
        if (!isSupplierValid) {
            throw new EntityNotFoundException(String.format("Supplier '%s' does not exist!", itemAddServiceModel.getSupplier()));
        }
    }


    private void validateCategory(ItemAddServiceModel itemAddServiceModel) {
        List<String> categories = this.categoryService.getAllCategoryNames();
        boolean isCategoryValid = categories.contains(itemAddServiceModel.getCategory());
        if (!isCategoryValid) {
            throw new EntityNotFoundException(String.format("Category '%s' does not exist!", itemAddServiceModel.getCategory()));
        }
    }
}
