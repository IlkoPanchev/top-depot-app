package warehouse.items.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.items.model.*;
import warehouse.users.model.UserServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
public interface ItemService {

    @Validated(OnCreate.class)
    ItemAddServiceModel add(@Valid ItemAddServiceModel itemAddServiceModel) throws IOException;

    Page<ItemViewServiceModel> findAllPageable(Pageable pageable);

    Page<ItemViewServiceModel> findAllPageableUnblocked(Pageable pageable);

    Page<ItemViewServiceModel> search(String keyword, Pageable pageable);

    Page<ItemViewServiceModel> searchUnblocked(String keyword, Pageable pageable);

    ItemViewServiceModel findById(Long id);

    boolean itemExists(String name);

    ItemViewServiceModel block(Long id);

    ItemViewServiceModel unblock(Long id);

    @Validated(OnUpdate.class)
    ItemAddServiceModel edit(@Valid ItemAddServiceModel itemAddServiceModel) throws IOException;

    HashMap<Integer, String> getTopItemsNamesMap(String fromDate, String toDate);

    HashMap<Integer, Integer> getTopItemsQuantityMap(String fromDate, String toDate);

    HashMap<Integer, BigDecimal> getTopItemsTurnoverMap(String fromDate, String toDate);

    void initItems();

    ItemEntity getById(long id);
}
