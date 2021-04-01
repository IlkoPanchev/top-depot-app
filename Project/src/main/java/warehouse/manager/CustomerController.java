package warehouse.manager;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.addresses.model.AddressServiceModel;
import warehouse.customers.model.CustomerAddBindingModel;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.model.CustomerTurnoverViewModel;
import warehouse.customers.model.CustomerViewBindingModel;
import warehouse.customers.service.CustomerService;
import warehouse.orders.service.OrderService;
import warehouse.pagination.PagerModel;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;


import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Controller
@RequestMapping("/customers")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@Validated
public class CustomerController {

    private static final String[] CUSTOMER_SORT_OPTIONS = {"Company", "Person", "Email", "Region", "City", "Street"};
    private static final Map<String, String> CUSTOMER_SORT_OPTIONS_MAP = Map.of("Company","companyName",
            "Person", "personName",
            "Email", "email",
            "Region", "addressEntity.region",
            "City", "addressEntity.city",
            "Street", "addressEntity.street");

    private final CustomerService customerService;
    private final ModelMapper modelMapper;
    private final TimeBordersConvertor timeBordersConvertor;
    private final OrderService orderService;

    @Autowired
    public CustomerController(CustomerService customerService, ModelMapper modelMapper, TimeBordersConvertor timeBordersConvertor, OrderService orderService) {
        this.customerService = customerService;
        this.modelMapper = modelMapper;
        this.timeBordersConvertor = timeBordersConvertor;
        this.orderService = orderService;
    }


    @GetMapping("/all/pageable")
    public String getAll(Model model,  @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                         @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         @RequestParam(name = "sortOption", defaultValue = "Company") String sortOption,
                         @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {

        page = getCurrentPageNumber(page);

        Page<CustomerViewBindingModel> customers = this.getPage(CUSTOMER_SORT_OPTIONS_MAP,sortOption, sortDirection, keyword, page, pageSize);

        PagerModel pager = new PagerModel(customers.getTotalPages(), customers.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("customers", customers);
        model.addAttribute("result", customers);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", CUSTOMER_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/customers/all/pageable");

        return "customers/customer-all";
    }

    @GetMapping("/all/newOrder")
    public String getAllNewOrder(Model model, @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                                 @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                                 @RequestParam(name = "page", defaultValue = "0") Integer page,
                                 @RequestParam(name = "sortOption", defaultValue = "Company") String sortOption,
                                 @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {


        page = getCurrentPageNumber(page);

        Page<CustomerViewBindingModel> customers = this.getPageUnblocked(CUSTOMER_SORT_OPTIONS_MAP,sortOption, sortDirection, keyword, page, pageSize);

        PagerModel pager = new PagerModel(customers.getTotalPages(), customers.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("customers", customers);
        model.addAttribute("result", customers);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", CUSTOMER_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/customers/all/newOrder");

        return "customers/customer-all-new-order";
    }

    @GetMapping("/add")
    public String add(Model model) {

        if (!model.containsAttribute("customerAddBindingModel")) {
            model.addAttribute("customerAddBindingModel", new CustomerAddBindingModel());
        }

        if (!model.containsAttribute("addressAddBindingModel")) {
            model.addAttribute("addressAddBindingModel", new AddressAddBindingModel());
        }

        if (!model.containsAttribute("customerExist")) {
            model.addAttribute("customerExist", false);
        }


        return "customers/customer-add";
    }

    @PostMapping("/add")
    @Validated(OnCreate.class)
    public String addConfirm(@Valid @ModelAttribute("customerAddBindingModel")
                                     CustomerAddBindingModel customerAddBindingModel,
                             BindingResult customerBindingResult,
                             @Valid @ModelAttribute("addressAddBindingModel")
                                     AddressAddBindingModel addressAddBindingModel,
                             BindingResult addressBindingResult,
                             RedirectAttributes redirectAttributes) {

        if (customerBindingResult.hasErrors() || addressBindingResult.hasErrors() ||
                (this.customerService.customerExists(customerAddBindingModel.getCompanyName())) && customerAddBindingModel.getId() == null) {

            if (this.customerService.customerExists(customerAddBindingModel.getCompanyName()) && customerAddBindingModel.getId() == null) {
                redirectAttributes.addFlashAttribute("customerExist", true);

            }
            redirectAttributes.addFlashAttribute("customerAddBindingModel", customerAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.customerAddBindingModel", customerBindingResult);

            redirectAttributes.addFlashAttribute("addressAddBindingModel", addressAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressAddBindingModel", addressBindingResult);

            return "redirect:add";
        }


        CustomerServiceModel customerServiceModel = this.modelMapper.map(customerAddBindingModel, CustomerServiceModel.class);
        AddressServiceModel addressServiceModel = this.modelMapper.map(addressAddBindingModel, AddressServiceModel.class);
        customerServiceModel.setAddress(addressServiceModel);

        this.customerService.add(customerServiceModel);

        return "redirect:/customers/all/pageable";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam("id") @Min(1) Long id){

        if (!model.containsAttribute("customerAddBindingModel")){
            CustomerServiceModel customerServiceModel = this.customerService.findById(id);
            CustomerAddBindingModel customerAddBindingModel = this.modelMapper
                    .map(customerServiceModel, CustomerAddBindingModel.class);

            model.addAttribute("customerAddBindingModel", customerAddBindingModel);
            model.addAttribute("addressAddBindingModel", customerAddBindingModel.getAddress());
        }

        return "customers/customer-edit";

    }




    @PatchMapping("/edit")
    @Validated(OnUpdate.class)
    public String editConfirm(@Valid @ModelAttribute("customerAddBindingModel")
                                          CustomerAddBindingModel customerAddBindingModel,
                              BindingResult customerBindingResult,
                              @Valid @ModelAttribute("addressAddBindingModel")
                                          AddressAddBindingModel addressAddBindingModel,
                              BindingResult addressBindingResult,
                              RedirectAttributes redirectAttributes){



        if (customerBindingResult.hasErrors() || addressBindingResult.hasErrors() ){
            redirectAttributes.addFlashAttribute("customerAddBindingModel", customerAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.customerAddBindingModel", customerBindingResult);

            redirectAttributes.addFlashAttribute("addressAddBindingModel", addressAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressAddBindingModel", addressBindingResult);

            redirectAttributes.addAttribute("id", customerAddBindingModel.getId());

            return "redirect:edit";

            }

        CustomerServiceModel customerServiceModel = this.modelMapper.map(customerAddBindingModel, CustomerServiceModel.class);
        AddressServiceModel addressServiceModel = this.modelMapper.map(addressAddBindingModel, AddressServiceModel.class);
        customerServiceModel.setAddress(addressServiceModel);
        customerServiceModel = this.customerService.edit(customerServiceModel);

        return "redirect:/customers/all/pageable";

    }

    @PostMapping("/block")
    public String block(@ModelAttribute(name="blockId") @Min(1) Long id){

        CustomerServiceModel customerServiceModel = this.customerService.block(id);

        return "redirect:/customers/all/pageable";
    }

    @PostMapping("/unblock")
    public String unblock(@ModelAttribute(name="unblockId") @Min(1) Long id){

        CustomerServiceModel customerServiceModel = this.customerService.unblock(id);

        return "redirect:/customers/all/pageable";
    }

    @GetMapping("/turnover")
    public String getSupplierTurnover(Model model, @RequestParam(name = "fromDate", defaultValue = "") String fromDate,
                                      @RequestParam(name = "toDate", defaultValue = "") String toDate,
                                      @RequestParam(name = "keyword", defaultValue = "null") String keyword){

        String[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsString(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());
        List<CustomerTurnoverViewModel> customers = this.customerService.getCustomerTurnover(keyword, fromDate, toDate);

        model.addAttribute("path", "/customers/turnover");
        model.addAttribute("customers", customers);
        model.addAttribute("fromDate", timeBorders[0]);
        model.addAttribute("toDate", timeBorders[1]);
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        return "customers/customer-turnover";
    }


    @GetMapping("/500")
    @PreAuthorize("hasRole('ADMIN')")
    public String testInternalError(){
        return "index";
    }



    private Page<CustomerViewBindingModel> getPage(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize) {

        String option = sortOptions.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<CustomerServiceModel> customerServiceModelPage;

        if (!keyword.equals("null")) {
            customerServiceModelPage = this.customerService.search(keyword, pageable);
        } else {
            customerServiceModelPage = this.customerService.findAllPageable(pageable);
        }

        List<CustomerViewBindingModel> customerViewBindingModels = customerServiceModelPage
                .stream()
                .map(customerServiceModel -> this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class))
                .collect(Collectors.toList());

        Page<CustomerViewBindingModel> pageToReturn = new PageImpl<>(customerViewBindingModels, pageable, customerServiceModelPage.getTotalElements());

        return pageToReturn;
    }

    private Page<CustomerViewBindingModel> getPageUnblocked(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize) {

        String option = sortOptions.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<CustomerServiceModel> customerServiceModelPage;

        if (!keyword.equals("null")) {
            customerServiceModelPage = this.customerService.searchUnblocked(keyword, pageable);
        } else {
            customerServiceModelPage = this.customerService.findAllPageableUnblocked(pageable);
        }

        List<CustomerViewBindingModel> customerViewBindingModels = customerServiceModelPage
                .stream()
                .map(customerServiceModel -> this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class))
                .collect(Collectors.toList());

        Page<CustomerViewBindingModel> pageToReturn = new PageImpl<>(customerViewBindingModels, pageable, customerServiceModelPage.getTotalElements());

        return pageToReturn;
    }

    private Integer getCurrentPageNumber(Integer page) {
        return   page < 1 ? 0 : page - 1;
    }


}
