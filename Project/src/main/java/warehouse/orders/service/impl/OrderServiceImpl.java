package warehouse.orders.service.impl;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import warehouse.constants.GlobalConstants;
import warehouse.customers.service.CustomerService;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.service.OrderLineService;
import warehouse.orders.model.OrderEntity;
import warehouse.orders.model.OrderAddServiceModel;
import warehouse.orders.model.OrderViewBindingModel;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.repository.OrderRepository;
import warehouse.orders.service.OrderService;
import warehouse.utils.file.FileIOUtil;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = Logger.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final TimeBordersConvertor timeBordersConvertor;
    private final Gson gson;
    private final FileIOUtil fileIOUtil;
    private final OrderLineService orderLineService;
    private final CustomerService customerService;
    private final ValidationUtil validationUtil;


    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            ModelMapper modelMapper,
                            TimeBordersConvertor timeBordersConvertor,
                            Gson gson,
                            FileIOUtil fileIOUtil,
                            @Lazy OrderLineService orderLineService,
                            CustomerService customerService,
                            ValidationUtil validationUtil) {
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
        this.timeBordersConvertor = timeBordersConvertor;
        this.gson = gson;
        this.fileIOUtil = fileIOUtil;
        this.orderLineService = orderLineService;
        this.customerService = customerService;
        this.validationUtil = validationUtil;
    }

    @Override
    public OrderAddServiceModel addOrder(OrderAddServiceModel orderAddServiceModel) {

        if (this.validationUtil.isValid(orderAddServiceModel)) {

            OrderEntity orderEntity = this.modelMapper.map(orderAddServiceModel, OrderEntity.class);

            orderEntity.setCreatedOn(LocalDateTime.now());
            orderEntity.setUpdatedOn(LocalDateTime.now());

            orderEntity = this.orderRepository.saveAndFlush(orderEntity);
            orderAddServiceModel = this.modelMapper.map(orderEntity, OrderAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(orderAddServiceModel));

        }

        return orderAddServiceModel;
    }

    @Override
    public OrderAddServiceModel editOrder(OrderAddServiceModel orderAddServiceModel) {

        if (this.validationUtil.isValid(orderAddServiceModel)) {

            OrderEntity orderEntity = this.modelMapper.map(orderAddServiceModel, OrderEntity.class);

            orderEntity.setUpdatedOn(LocalDateTime.now());

            orderEntity = this.orderRepository.saveAndFlush(orderEntity);
            orderAddServiceModel = this.modelMapper.map(orderEntity, OrderAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(orderAddServiceModel));

        }

        return orderAddServiceModel;
    }

    @Override
    public OrderAddServiceModel completeOrder(OrderAddServiceModel orderAddServiceModel) {

        if (this.validationUtil.isValid(orderAddServiceModel)) {

            OrderEntity orderEntity = this.modelMapper.map(orderAddServiceModel, OrderEntity.class);

            orderEntity.setClosed(true);
            orderEntity.setUpdatedOn(LocalDateTime.now());

            orderEntity = this.orderRepository.saveAndFlush(orderEntity);
            orderAddServiceModel = this.modelMapper.map(orderEntity, OrderAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(orderAddServiceModel));

        }

        return orderAddServiceModel;
    }

    @Override
    public OrderAddServiceModel incompleteOrder(OrderAddServiceModel orderAddServiceModel) {

        if (this.validationUtil.isValid(orderAddServiceModel)) {

            OrderEntity orderEntity = this.modelMapper.map(orderAddServiceModel, OrderEntity.class);

            orderEntity.setClosed(false);
            orderEntity.setUpdatedOn(LocalDateTime.now());

            orderEntity = this.orderRepository.saveAndFlush(orderEntity);
            orderAddServiceModel = this.modelMapper.map(orderEntity, OrderAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(orderAddServiceModel));

        }

        return orderAddServiceModel;
    }

    @Override
    public OrderAddServiceModel archiveOrder(OrderAddServiceModel orderAddServiceModel) {

        if (this.validationUtil.isValid(orderAddServiceModel)) {

            OrderEntity orderEntity = this.modelMapper.map(orderAddServiceModel, OrderEntity.class);

            orderEntity.setArchives(true);
            orderEntity.setUpdatedOn(LocalDateTime.now());

            orderEntity = this.orderRepository.saveAndFlush(orderEntity);
            orderAddServiceModel = this.modelMapper.map(orderEntity, OrderAddServiceModel.class);

        } else {

            throw new ConstraintViolationException(this.validationUtil.getViolations(orderAddServiceModel));

        }

        return orderAddServiceModel;
    }

    @Override
    public long getRepositoryCount() {
        return this.orderRepository.count();
    }

    @Override
    public Page<OrderViewServiceModel> findAllPageable(Pageable pageable) {

        Page<OrderEntity> orderEntities = this.orderRepository.findAllByDeletedFalse(pageable);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntities.stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());

        Page<OrderViewServiceModel> orderViewServiceModelsPage = new PageImpl<>(orderViewServiceModels, pageable, orderEntities.getTotalElements());

        return orderViewServiceModelsPage;
    }

    @Override
    public Page<OrderViewServiceModel> search(String keyword, Pageable pageable) {

        Page<OrderEntity> orderEntities = this.orderRepository.search(keyword, pageable);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntities.stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());
        Page<OrderViewServiceModel> orderViewServiceModelsPage = new PageImpl<>(orderViewServiceModels, pageable, orderEntities.getTotalElements());
        return orderViewServiceModelsPage;
    }

    @Override
    public List<OrderViewServiceModel> findAllPageableOrderByUpdated() {


        Pageable pageable = PageRequest.of(0, 5);

        Page<OrderEntity> orderEntityList = this.orderRepository.findAllOrderByUpdatedOnDesc(pageable);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntityList
                .stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());

        return orderViewServiceModels;
    }

    @Override
    public List<OrderViewServiceModel> findAllPageableCompletedOrderByUpdated() {


        Pageable pageable = PageRequest.of(0, 5);

        Page<OrderEntity> orderEntityList = this.orderRepository.findAllCompletedOrdersByUpdatedOnDesc(pageable);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntityList
                .stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());

        return orderViewServiceModels;
    }

    @Override
    public List<OrderViewServiceModel> findAllPageableOrderByCreated() {

        Pageable pageable = PageRequest.of(0, 5);

        Page<OrderEntity> orderEntityList = this.orderRepository.findAllOrdersByCreatedOnDesc(pageable);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntityList
                .stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());

        return orderViewServiceModels;
    }

    @Override
    public OrderViewServiceModel findById(Long id) {

        OrderEntity orderEntity = this.orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        OrderViewServiceModel orderViewServiceModel = this.modelMapper.map(orderEntity, OrderViewServiceModel.class);

        return (orderViewServiceModel);
    }

    @Override
    public HashMap<LocalDate, BigDecimal> getLastWeek() {

        HashMap<LocalDate, BigDecimal> lastWeekMap = new LinkedHashMap<>();

        LocalDateTime[] startEnd = this.getStartAndEndOfLastWeek();
        LocalDateTime weekStart = startEnd[0];
        LocalDateTime weekEnd = startEnd[1];

        List<OrderEntity> lastWeekOrders = this.orderRepository.findAllByUpdatedOnBetweenAndArchivesTrueOrderByUpdatedOnAsc(weekStart, weekEnd);

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.plusDays(i).toLocalDate();
            lastWeekMap.put(currentDate, new BigDecimal(String.valueOf("0")));
        }

        for (OrderEntity order : lastWeekOrders) {
            LocalDate orderDate = order.getUpdatedOn().toLocalDate();
            BigDecimal newTotal = order.getTotal();
            BigDecimal currentTotal = lastWeekMap.get(orderDate);
            BigDecimal total = currentTotal.add(newTotal);
            lastWeekMap.put(orderDate, total);
        }
        return lastWeekMap;
    }

    @Override
    public HashMap<Integer, String> getWeekDatesMap() {

        HashMap<Integer, String> weekDatesMap = new LinkedHashMap<>();

        int key = 0;

        HashMap<LocalDate, BigDecimal> lastWeekMap = this.getLastWeek();

        for (Map.Entry<LocalDate, BigDecimal> entry : lastWeekMap.entrySet()) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            weekDatesMap.put(++key, dateTimeFormatter.format(entry.getKey()));
        }
        return weekDatesMap;
    }

    @Override
    public HashMap<Integer, BigDecimal> getWeekDatesTurnoverMap() {

        HashMap<Integer, BigDecimal> weekDatesTurnoverMap = new LinkedHashMap<>();

        int key = 0;

        HashMap<LocalDate, BigDecimal> lastWeekMap = this.getLastWeek();

        for (Map.Entry<LocalDate, BigDecimal> entry : lastWeekMap.entrySet()) {
            weekDatesTurnoverMap.put(++key, entry.getValue());
        }
        return weekDatesTurnoverMap;
    }

    @Override
    public LocalDateTime getDateTimeFirstArchiveOrder() {

        List<OrderEntity> orderEntities = this.orderRepository.findAllByArchivesTrueOrderByUpdatedOnAsc();

        LocalDateTime localDateTime;
        if (orderEntities.isEmpty()) {
            localDateTime = LocalDateTime.now();
        } else {
            localDateTime = orderEntities.get(0).getUpdatedOn();
        }

        return localDateTime;
    }

    @Override
    public LocalDateTime getDateTimeFirstCreatedOrder() {

        List<OrderEntity> orderEntities = this.orderRepository.findAllByOrderByCreatedOnAsc();


        LocalDateTime localDateTime;
        if (orderEntities.isEmpty()) {
            localDateTime = LocalDateTime.now();
        } else {
            localDateTime = orderEntities.get(0).getCreatedOn();
        }


        return localDateTime;
    }

    @Override
    public HashMap<String, Integer> getPieChartMap(String fromDate, String toDate) {

        HashMap<String, Integer> orderMap = new HashMap<>();

        LocalDateTime[] timeBordersDateTime = this.timeBordersConvertor
                .getTimeBordersAsLocalDateTime(fromDate, toDate, this.getDateTimeFirstCreatedOrder());

        int created = this.orderRepository
                .findAllByCreatedOnBetweenAndClosedFalseAndArchivesFalseAndDeletedFalse(timeBordersDateTime[0],
                        timeBordersDateTime[1]).size();
        orderMap.put("created", created);

        int completed = this.orderRepository
                .findAllByUpdatedOnBetweenAndClosedTrueAndArchivesFalse(timeBordersDateTime[0],
                        timeBordersDateTime[1]).size();
        orderMap.put("completed", completed);

        int archive = this.orderRepository
                .findAllByUpdatedOnBetweenAndClosedTrueAndArchivesTrue(timeBordersDateTime[0],
                        timeBordersDateTime[1]).size();
        orderMap.put("archive", archive);

        int totalOrders = created + completed + archive;

        orderMap.put("totalOrders", totalOrders);

        return orderMap;
    }


    @Override
    public HashMap<String, String> getTimeBordersMap(String fromDate, String toDate) {

        HashMap<String, String> timeBordersMap = new HashMap<>();

        String[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsString(fromDate, toDate, this.getDateTimeFirstCreatedOrder());

        timeBordersMap.put("fromDate", timeBorders[0]);
        timeBordersMap.put("toDate", timeBorders[1]);

        return timeBordersMap;

    }

    @Override
    public List<OrderViewServiceModel> getAllByUpdatedOnBeforeAndClosedFalseAndArchivesFalse(LocalDateTime upTo) {

        List<OrderEntity> orderEntityList = this.orderRepository.findAllByUpdatedOnBeforeAndClosedFalseAndArchivesFalseAndDeletedFalse(upTo);

        List<OrderViewServiceModel> orderViewServiceModels = orderEntityList
                .stream()
                .map(orderEntity -> this.modelMapper.map(orderEntity, OrderViewServiceModel.class))
                .collect(Collectors.toList());
        return orderViewServiceModels;
    }

    @Override
    public void markAsDeleted(Long id) {

        OrderEntity orderEntity = this.orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        orderEntity.setDeleted(true);
        this.orderRepository.saveAndFlush(orderEntity);
    }

    @Override
    public void exportArchivedOrder(OrderViewBindingModel orderViewBindingModel) throws IOException {

        String json = this.gson.toJson(orderViewBindingModel);
        this.fileIOUtil.write(json, GlobalConstants.EXPORT_FILE_PATH + "order_" + orderViewBindingModel.getId() + ".json");
        logger.info(String.format("Exported file - order %s", orderViewBindingModel.getId()));
    }

    @Override
    @Transactional
    public void initOrders() {

        if (this.orderRepository.count() == 0) {

            for (int i = 1; i < INIT_COUNT; i++) {

                OrderEntity orderEntity = new OrderEntity();

                Set<OrderLineEntity> orderLineEntities = new HashSet<>();
                orderLineEntities.add(this.orderLineService.getById(i));
                orderEntity.setOrderLineEntities(orderLineEntities);

                orderEntity.setCustomer(this.customerService.getById(i));
                orderEntity.setTotal(orderLineEntities.stream()
                        .map(OrderLineEntity::getSubtotal).reduce(new BigDecimal(0), BigDecimal::add));

                orderEntity.setCreatedOn(LocalDateTime.now());
                orderEntity.setUpdatedOn(LocalDateTime.now());

                if (i % 2 == 0 && i > 2) {
                    orderEntity.setClosed(true);
                }

                if (i % 2 != 0) {
                    orderEntity.setClosed(true);
                    orderEntity.setArchives(true);


                }

                this.orderRepository.saveAndFlush(orderEntity);

            }

            List<OrderEntity> archivedOrderEntities = this.orderRepository.findAllByArchivesTrueOrderByUpdatedOnAsc();
            LocalDateTime[] startEnd = this.getStartAndEndOfLastWeek();
            LocalDateTime weekStart = startEnd[0];

            int counter = 1;

            for (OrderEntity archivedOrderEntity : archivedOrderEntities) {
                archivedOrderEntity.setUpdatedOn(weekStart.plusDays(++counter));
                this.orderRepository.saveAndFlush(archivedOrderEntity);
            }
        }
    }

    private LocalDateTime[] getStartAndEndOfLastWeek() {

        LocalDateTime[] startEnd = new LocalDateTime[2];

        LocalDateTime now = LocalDateTime.now();
        LocalDate localDateNow = now.toLocalDate();
        LocalDateTime nowAtStartOfDay = localDateNow.atStartOfDay();

        LocalDateTime weekStart = nowAtStartOfDay.minusDays(7 + nowAtStartOfDay.getDayOfWeek().getValue() - 1);
        startEnd[0] = weekStart;
        LocalDateTime weekEnd = nowAtStartOfDay.minusDays(nowAtStartOfDay.getDayOfWeek().getValue() - 1);
        startEnd[1] = weekEnd;

        return startEnd;
    }

}
