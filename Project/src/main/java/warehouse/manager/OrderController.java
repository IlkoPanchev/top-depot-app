package warehouse.manager;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.model.CustomerViewBindingModel;
import warehouse.customers.service.CustomerService;
import warehouse.events.order.ArchiveOrderPublisher;
import warehouse.items.model.ItemViewBindingModel;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.items.service.ItemService;
import warehouse.orderline.model.OrderLineViewBindingModel;
import warehouse.orders.orderdata.OrderData;
import warehouse.orders.orderdata.service.OrderDataManager;
import warehouse.orders.model.OrderAddBindingModel;
import warehouse.orders.model.OrderAddServiceModel;
import warehouse.orders.model.OrderViewBindingModel;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.service.OrderService;
import warehouse.pagination.PagerModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
@Validated
public class OrderController {

    private static final String[] ORDER_SORT_OPTIONS = {"Updated", "Created", "Company", "Person", "Completed", "Archive", "Total"};
    private static final Map<String, String> ORDER_SORT_OPTIONS_MAP = Map.of("Updated", "updatedOn",
            "Created", "createdOn",
            "Company", "customer.companyName",
            "Person", "customer.personName",
            "Completed", "closed",
            "Archive", "archives",
            "Total", "total");

    private final OrderService orderService;
    private final OrderDataManager orderDataManager;
    private final CustomerService customerService;
    private final ItemService itemService;
    private final ModelMapper modelMapper;
    private final ArchiveOrderPublisher archiveOrderPublisher;


    @Autowired
    public OrderController(OrderService orderService,
                           OrderDataManager orderDataManager,
                           CustomerService customerService,
                           ItemService itemService,
                           ModelMapper modelMapper, ArchiveOrderPublisher archiveOrderPublisher) {
        this.orderService = orderService;
        this.orderDataManager = orderDataManager;
        this.customerService = customerService;
        this.itemService = itemService;
        this.modelMapper = modelMapper;
        this.archiveOrderPublisher = archiveOrderPublisher;
    }


    @GetMapping("/all/pageable")
    @PreAuthorize("hasRole('ROLE_USER')")
    public String allPageable(Model model,
                              @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                              @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                              @RequestParam(name = "page", defaultValue = "0") Integer page,
                              @RequestParam(name = "sortOption", defaultValue = "Updated") String sortOption,
                              @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection) {

        page = page < 1 ? 0 : page - 1;
        String option = ORDER_SORT_OPTIONS_MAP.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<OrderViewServiceModel> orderViewServiceModelsPage;

        if (!keyword.equals("null")) {
            orderViewServiceModelsPage = this.orderService.search(keyword, pageable);
        } else {
            orderViewServiceModelsPage = this.orderService.findAllPageable(pageable);
        }

        List<OrderViewBindingModel> orderViewBindingModels = orderViewServiceModelsPage
                .stream()
                .map(orderViewServiceModel -> this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class))
                .collect(Collectors.toList());

        Page<OrderViewBindingModel> orders = new PageImpl<>(orderViewBindingModels, pageable, orderViewServiceModelsPage.getTotalElements());

        PagerModel pager = new PagerModel(orders.getTotalPages(), orders.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("orders", orders);
        model.addAttribute("result", orders);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", ORDER_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/orders/all/pageable");


        return "orders/order-all";
    }

    @GetMapping("/open")
    public String add(Model model, HttpSession httpSession, @RequestParam("id") @Min(1) Long id) {

        this.orderDataManager.removeOrderData(httpSession);
        OrderData orderData = this.orderDataManager.getOrderData(httpSession);
        CustomerServiceModel customerServiceModel = this.customerService.findById(id);
        CustomerViewBindingModel customerViewBindingModel = this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class);
        orderData.setCustomer(customerViewBindingModel);

        if (!model.containsAttribute("notEnoughStock")) {
            model.addAttribute("notEnoughStock", 0);
        }

        return "orders/order-add";
    }

    @GetMapping("/open-not-stock")
    public String addNotStock(Model model, HttpSession httpSession, @RequestParam("id") @Min(1) Long id) {

        OrderData orderData = this.orderDataManager.getOrderData(httpSession);
        CustomerServiceModel customerServiceModel = this.customerService.findById(id);
        CustomerViewBindingModel customerViewBindingModel = this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class);
        orderData.setCustomer(customerViewBindingModel);

        if (!model.containsAttribute("notEnoughStock")) {
            model.addAttribute("notEnoughStock", 0);
        }

        return "orders/order-add";
    }


    @GetMapping("/addItem")
    public String addItem(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("itemViewBindingModel")) {

            ItemViewServiceModel itemViewServiceModel = this.itemService.findById(id);
            ItemViewBindingModel itemViewBindingModel = this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class);
            model.addAttribute("itemViewBindingModel", itemViewBindingModel);
        }

        if (!model.containsAttribute("isStockEnough")) {

            model.addAttribute("isStockEnough", true);
        }

        if (!model.containsAttribute("quantity")) {

            model.addAttribute("quantity", 0);
        }


        return "orders/order-add-item";
    }

    @PostMapping("/addItem")
    public String addItemConfirm(@RequestParam("id") @Min(1) Long id,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession httpSession,
                                 RedirectAttributes redirectAttributes) {

        OrderData orderData = this.orderDataManager.getOrderData(httpSession);
        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(id);
        ItemViewBindingModel itemViewBindingModel = this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class);

        if (this.itemService.isStockEnough(id, quantity)) {

            orderData.addOrderLine(itemViewBindingModel, quantity);

            return "orders/order-add";
        }

        redirectAttributes.addFlashAttribute("isStockEnough", false);
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("quantity", quantity);
        return "redirect:addItem";
    }

    @GetMapping("/addItem/cancel")
    public String addItemCancel() {

        return "orders/order-add";
    }

    @GetMapping("/orderData/update")
    public String updateOrderData(@RequestParam("id") @Min(1) Long id, @RequestParam("quantity") int quantity, HttpSession httpSession) {

        OrderData orderData = this.orderDataManager.getOrderData(httpSession);
        orderData.updateOrderLine(id, quantity);

        return "fragments/draft-order-order-lines-table :: draftOrderOrderLinesTable";
    }

    @PostMapping("/orderData/remove")
    public String removeOrderData(@RequestParam("removeId") Long id, HttpSession httpSession) {

        OrderData orderData = this.orderDataManager.getOrderData(httpSession);
        orderData.removeOrderLine(id);

        return "orders/order-add";
    }

    @GetMapping("/cancel")
    public String cancel(HttpSession httpSession) {

        this.orderDataManager.removeOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/save")
    public String save(HttpSession httpSession,
                       HttpServletRequest httpServletRequest,
                       RedirectAttributes redirectAttributes) {

        OrderData orderData = this.orderDataManager.getOrderData(httpSession);

        Set<OrderLineViewBindingModel> orderLineViewBindingModels = orderData.getOrderLineEntities();
        for (OrderLineViewBindingModel orderLineViewBindingModel : orderLineViewBindingModels) {
            boolean isStockEnough = this.itemService.isStockEnough(orderLineViewBindingModel.getItem().getId(),
                    orderLineViewBindingModel.getQuantity());
            if (!isStockEnough) {
                redirectAttributes.addAttribute("id", orderData.getCustomer().getId());
                redirectAttributes.addFlashAttribute("notEnoughStock", orderLineViewBindingModel.getItem().getId());
                return "redirect:open-not-stock";
            }
        }

        OrderAddBindingModel orderAddBindingModel = this.modelMapper.map(orderData, OrderAddBindingModel.class);
        OrderAddServiceModel orderAddServiceModel = this.modelMapper.map(orderAddBindingModel, OrderAddServiceModel.class);

        this.orderService.addOrder(orderAddServiceModel);
        this.itemService.saveOrderUpdateStock(orderAddServiceModel);
        this.orderDataManager.removeOrderData(httpSession);
        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/save/draft")
    public String saveDraft() {

        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/open/draft")
    public String openDraft() {

        return "orders/order-add";
    }

    @GetMapping("/edit")
    public String editOrder(Model model, @RequestParam("id") @Min(1) Long id, HttpSession httpSession) {


        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(id);

        this.orderDataManager.removeEditOrderData(httpSession);
        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);
        OrderData orderData = this.modelMapper.map(orderViewBindingModel, OrderData.class);
        //set order by OrderLineViewBindingModel.getName
        Comparator<OrderLineViewBindingModel> comparator = Comparator.comparing((OrderLineViewBindingModel o) -> o.getItem().getName());
        Set<OrderLineViewBindingModel> orderLineViewBindingModels = new TreeSet<>(comparator);
        orderLineViewBindingModels.addAll(orderData.getOrderLineEntities());
        orderData.setOrderLineEntities(orderLineViewBindingModels);

        this.orderDataManager.setEditOrderData(httpSession, orderData);

        return "orders/order-edit";

    }

    @GetMapping("/edit-not-stock")
    public String editOrderNotStock(Model model, @RequestParam("id") @Min(1) Long id, HttpSession httpSession) {


        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(id);

        this.orderDataManager.removeEditOrderData(httpSession);
        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);
        OrderData orderData = this.modelMapper.map(orderViewBindingModel, OrderData.class);
        //set order by OrderLineViewBindingModel.getName
        Comparator<OrderLineViewBindingModel> comparator = Comparator.comparing((OrderLineViewBindingModel o) -> o.getItem().getName());
        Set<OrderLineViewBindingModel> orderLineViewBindingModels = new TreeSet<>(comparator);
        orderLineViewBindingModels.addAll(orderData.getOrderLineEntities());
        orderData.setOrderLineEntities(orderLineViewBindingModels);

        if (!model.containsAttribute("notEnoughStock")) {
            model.addAttribute("notEnoughStock", 0);
        }

        this.orderDataManager.setEditOrderData(httpSession, orderData);

        return "orders/order-edit";

    }

    @GetMapping("/editOrder/update")
    public String editOrderUpdateOrderData(@RequestParam("id") @Min(1) Long id, @RequestParam("quantity") int quantity, HttpSession httpSession) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);
        orderData.updateOrderLine(id, quantity);

        return "fragments/order-edit-order-lines-table :: editOrderOrderLinesTable";
    }

    @PostMapping("/editOrder/remove")
    public String editOrderRemoveOrderData(@RequestParam("removeId") Long id, HttpSession httpSession) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);
        orderData.removeOrderLine(id);

        return "orders/order-edit";
    }

    @GetMapping("/editOrder/addItem")
    public String editOrderAddItem(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("itemViewBindingModel")) {
            ItemViewServiceModel itemViewServiceModel = this.itemService.findById(id);
            ItemViewBindingModel itemViewBindingModel = this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class);
            model.addAttribute("itemViewBindingModel", itemViewBindingModel);
        }


        if (!model.containsAttribute("isStockEnough")) {

            model.addAttribute("isStockEnough", true);
        }

        if (!model.containsAttribute("quantity")) {

            model.addAttribute("quantity", 0);
        }


        return "orders/order-add-item-edit-order";
    }

    @PostMapping("/editOrder/addItem")
    public String editOrderAddItemConfirm(@RequestParam("id") @Min(1) Long id,
                                          @RequestParam("quantity") int quantity,
                                          HttpSession httpSession,
                                          RedirectAttributes redirectAttributes) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);
        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(id);
        ItemViewBindingModel itemViewBindingModel = this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class);

        if (this.itemService.isStockEnough(id, quantity)) {

            orderData.addOrderLine(itemViewBindingModel, quantity);

            return "orders/order-edit";
        }

        redirectAttributes.addFlashAttribute("isStockEnough", false);
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addFlashAttribute("quantity", quantity);
        return "redirect:/orders/editOrder/addItem";
    }

    @GetMapping("/editOrder/addItem/cancel")
    public String editOrderAddItemCancel() {

        return "orders/order-edit";
    }

    @GetMapping("/edit/save")
    public String editOrderSave(HttpSession httpSession,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);

        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(orderData.getId());

        Set<OrderLineViewBindingModel> orderLineViewBindingModels = orderData.getOrderLineEntities();

        for (OrderLineViewBindingModel orderLineViewBindingModel : orderLineViewBindingModels) {

            boolean isStockEnough = this.itemService.isStockEnoughEditOrder(orderViewServiceModel,
                    orderLineViewBindingModel.getId(),
                    orderLineViewBindingModel.getQuantity());
            if (!isStockEnough) {
                redirectAttributes.addAttribute("id", orderData.getId());
                redirectAttributes.addFlashAttribute("notEnoughStock", orderLineViewBindingModel.getItem().getId());
                return "redirect:/orders/edit-not-stock";
            }
        }

        OrderAddBindingModel orderAddBindingModel = this.modelMapper.map(orderData, OrderAddBindingModel.class);
        OrderAddServiceModel orderAddServiceModel = this.modelMapper.map(orderAddBindingModel, OrderAddServiceModel.class);

        this.orderService.editOrder(orderAddServiceModel);

        this.itemService.editOrderUpdateStock(orderViewServiceModel, orderAddServiceModel);
        this.orderDataManager.removeEditOrderData(httpSession);
        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/edit/cancel")
    public String editCancel(HttpSession httpSession) {

        this.orderDataManager.removeEditOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/complete/open")
    @PreAuthorize("hasRole('USER')")
    public String orderCompleteOpen(Model model, @RequestParam("id") @Min(1) Long id, HttpSession httpSession) {

        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(id);

        this.orderDataManager.removeEditOrderData(httpSession);

        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);

        OrderData orderData = this.modelMapper.map(orderViewBindingModel, OrderData.class);
        this.orderDataManager.setEditOrderData(httpSession, orderData);

        return "orders/order-complete-open";
    }

    @GetMapping("/archive/open")
    @PreAuthorize("hasRole('USER')")
    public String orderArchiveOpen(Model model, @RequestParam("id") @Min(1) Long id, HttpSession httpSession) {

        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(id);

        this.orderDataManager.removeEditOrderData(httpSession);

        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);

        OrderData orderData = this.modelMapper.map(orderViewBindingModel, OrderData.class);
        this.orderDataManager.setEditOrderData(httpSession, orderData);

        return "orders/order-archive-open";
    }

    @GetMapping("/complete")
    @PreAuthorize("hasRole('USER')")
    public String orderComplete(HttpSession httpSession, HttpServletRequest httpServletRequest) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);

        OrderAddBindingModel orderAddBindingModel = this.modelMapper.map(orderData, OrderAddBindingModel.class);
        OrderAddServiceModel orderAddServiceModel = this.modelMapper.map(orderAddBindingModel, OrderAddServiceModel.class);

        this.orderService.completeOrder(orderAddServiceModel);
        this.orderDataManager.removeEditOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }

    @GetMapping("/incomplete")
    @PreAuthorize("hasRole('USER')")
    public String orderIncomplete(HttpSession httpSession, HttpServletRequest httpServletRequest) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);

        OrderAddBindingModel orderAddBindingModel = this.modelMapper.map(orderData, OrderAddBindingModel.class);
        OrderAddServiceModel orderAddServiceModel = this.modelMapper.map(orderAddBindingModel, OrderAddServiceModel.class);

        this.orderService.incompleteOrder(orderAddServiceModel);
        this.orderDataManager.removeEditOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }

    @GetMapping("/archive")
    @PreAuthorize("hasRole('USER')")
    public String orderArchive(HttpSession httpSession, HttpServletRequest httpServletRequest) {

        OrderData orderData = this.orderDataManager.getEditOrderData(httpSession);

        OrderAddBindingModel orderAddBindingModel = this.modelMapper.map(orderData, OrderAddBindingModel.class);
        OrderAddServiceModel orderAddServiceModel = this.modelMapper.map(orderAddBindingModel, OrderAddServiceModel.class);


        this.orderService.archiveOrder(orderAddServiceModel);
        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderAddServiceModel, OrderViewBindingModel.class);

        this.archiveOrderPublisher.publishOrderArchived(orderViewBindingModel);

        this.orderDataManager.removeEditOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/complete/cancel")
    @PreAuthorize("hasRole('USER')")
    public String completeCancel(HttpSession httpSession) {

        this.orderDataManager.removeEditOrderData(httpSession);

        return "redirect:/orders/all/pageable";
    }


    @GetMapping("/archive/view")
    @PreAuthorize("hasRole('USER')")
    public String orderArchiveView(Model model, @RequestParam("id") @Min(1) Long id) {


        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(id);
        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);

        model.addAttribute("orderViewBindingModel", orderViewBindingModel);

        return "orders/order-archive-view";

    }

    @GetMapping("/archive/back")
    @PreAuthorize("hasRole('USER')")
    public String archiveViewBack() {

        return "redirect:/orders/all/pageable";
    }

    @GetMapping("/pieChart")
    @PreAuthorize("hasRole('USER')")
    public String getPieChart(Model model, @RequestParam(name = "fromDate", defaultValue = "") String fromDate,
                              @RequestParam(name = "toDate", defaultValue = "") String toDate) {

        HashMap<String, Integer> orderMap = this.orderService.getPieChartMap(fromDate, toDate);
        HashMap<String, String> timeBordersMap = this.orderService.getTimeBordersMap(fromDate, toDate);
        model.addAttribute("totalOrders", orderMap.get("totalOrders"));
        model.addAttribute("orderMap", orderMap);
        model.addAttribute("timeBordersMap", timeBordersMap);
        model.addAttribute("path", "/orders/pieChart");
        model.addAttribute("fromDate", timeBordersMap.get("fromDate"));
        model.addAttribute("toDate", timeBordersMap.get("toDate"));

        return "orders/order-pie-chart";
    }

    @GetMapping("/areaChart")
    public String getSalesLastWeek(Model model) {

        this.orderService.getLastWeek();
        model.addAttribute("ordersLastWeekDatesMap", this.orderService.getWeekDatesMap());
        model.addAttribute("ordersLastWeekTurnoverMap", this.orderService.getWeekDatesTurnoverMap());

        return "orders/order-area-chart";
    }


}
