package warehouse.orders.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import warehouse.orders.model.OrderAddServiceModel;
import warehouse.orders.model.OrderEntity;
import warehouse.orders.model.OrderViewBindingModel;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnCreateOrder;
import warehouse.validated.OnUpdate;
import warehouse.validated.OnUpdateOrder;

import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
@Validated
public interface OrderService {

    @Validated(OnCreateOrder.class)
    OrderAddServiceModel addOrder(@Valid OrderAddServiceModel orderAddServiceModel);

    @Validated(OnUpdateOrder.class)
    OrderAddServiceModel editOrder(@Valid OrderAddServiceModel orderAddServiceModel);

    Page<OrderViewServiceModel> search(String keyword, Pageable pageable);

    List<OrderViewServiceModel> findAllPageableOrderByUpdated();

    List<OrderViewServiceModel> findAllPageableCompletedOrderByUpdated();

    List<OrderViewServiceModel>findAllPageableOrderByCreated();

    OrderViewServiceModel findById(Long id);

    @Validated(OnUpdateOrder.class)
    OrderAddServiceModel completeOrder(@Valid OrderAddServiceModel orderAddServiceModel);

    @Validated(OnUpdateOrder.class)
    OrderAddServiceModel incompleteOrder(@Valid OrderAddServiceModel orderAddServiceModel);

    @Validated(OnUpdateOrder.class)
    OrderAddServiceModel archiveOrder(@Valid OrderAddServiceModel orderAddServiceModel);

    long getRepositoryCount();

    Page<OrderViewServiceModel> findAllPageable(Pageable pageable);

    HashMap<String, Integer> getPieChartMap(String fromDate, String toDate);

    HashMap<LocalDate, BigDecimal> getLastWeek();

    HashMap<Integer, String> getWeekDatesMap();

    HashMap<Integer, BigDecimal> getWeekDatesTurnoverMap();

    LocalDateTime getDateTimeFirstArchiveOrder();

    LocalDateTime getDateTimeFirstCreatedOrder();

    HashMap<String, String> getTimeBordersMap(String fromDate, String toDate);

    List<OrderViewServiceModel> getAllByUpdatedOnBeforeAndClosedFalseAndArchivesFalse(LocalDateTime upTo);

    void markAsDeleted(Long id);

    void exportArchivedOrder(OrderViewBindingModel orderViewBindingModel) throws IOException;

    void initOrders();
}
