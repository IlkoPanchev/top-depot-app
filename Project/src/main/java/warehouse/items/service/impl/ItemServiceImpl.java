package warehouse.items.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class ItemServiceImpl implements ItemService {
    private final ModelMapper modelMapper;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CloudinaryService cloudinaryService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final OrderService orderService;
    private final TimeBordersConvertor timeBordersConvertor;
    private final ValidationUtil validationUtil;

    @Autowired
    public ItemServiceImpl(ModelMapper modelMapper,
                           ItemRepository itemRepository,
                           CategoryRepository categoryRepository,
                           SupplierRepository supplierRepository,
                           CloudinaryService cloudinaryService,
                           CategoryService categoryService,
                           SupplierService supplierService,
                           OrderService orderService,
                           TimeBordersConvertor timeBordersConvertor, ValidationUtil validationUtil) {
        this.modelMapper = modelMapper;
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.cloudinaryService = cloudinaryService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.orderService = orderService;
        this.timeBordersConvertor = timeBordersConvertor;
        this.validationUtil = validationUtil;
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

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1]);

        return this.timeBordersConvertor.getBordersAndNamesMap(itemsNamesMap, timeBordersPieChart, result);
    }


    @Override
    public HashMap<Integer, Integer> getTopItemsQuantityMap(String fromDate, String toDate) {

        HashMap<Integer, Integer> itemsQuantityMap = new HashMap<>();

        LocalDateTime[] timeBorders = this.timeBordersConvertor.
                getTimeBordersAsLocalDateTime(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1]);

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

        List<Object[]> result = this.itemRepository.findTopItems(timeBorders[0], timeBorders[1]);

        int key = 2;
        for (Object[] objects : result) {
            itemsTurnoverMap.put(++key, new BigDecimal(String.valueOf(objects[2])));
        }

        return itemsTurnoverMap;
    }

    @Override
    @Transactional
    public void initItems() {

        if (this.itemRepository.count() == 0) {

            for (int i = 1; i < INIT_COUNT; i++) {

                ItemEntity itemEntity = new ItemEntity();
                itemEntity.setName(String.format("Name_%d", i));
                itemEntity.setDescription(String.format("description_%d", i));
                itemEntity.setPrice((BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(i)));
                itemEntity.setLocation(String.format("Location_%d", i));
                itemEntity.setCategory(this.categoryService.getById(i));
                itemEntity.setSupplier(this.supplierService.getById(i));
                itemEntity.setImg("http://res.cloudinary.com/ipanchev/image/upload/v1616226988/id08tagytlyglzz84nyb.jpg");

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
