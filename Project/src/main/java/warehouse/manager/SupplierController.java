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
import warehouse.addresses.service.AddressService;
import warehouse.items.service.ItemService;
import warehouse.orders.service.OrderService;
import warehouse.pagination.PagerModel;
import warehouse.suppliers.model.SupplierAddBindingModel;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.model.SupplierTurnoverViewModel;
import warehouse.suppliers.model.SupplierViewBindingModel;
import warehouse.suppliers.service.SupplierService;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Controller
@RequestMapping("/suppliers")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@Validated
public class SupplierController {

    private static final String[] SUPPLIER_SORT_OPTIONS = {"Name", "Email", "Region", "City", "Street"};
    private static final Map<String, String> SUPPLIER_SORT_OPTIONS_MAP = Map.of("Name","name",
            "Email", "email",
            "Region", "addressEntity.region",
            "City", "addressEntity.city",
            "Street", "addressEntity.street");

    private final SupplierService supplierService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final ModelMapper modelMapper;
    private final TimeBordersConvertor timeBordersConvertor;

    @Autowired
    public SupplierController(SupplierService supplierService,
                              ItemService itemService,
                              OrderService orderService,
                              AddressService addressService,
                              ModelMapper modelMapper,
                              TimeBordersConvertor timeBordersConvertor) {

        this.supplierService = supplierService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.modelMapper = modelMapper;
        this.timeBordersConvertor = timeBordersConvertor;
    }

    @GetMapping("/add")
    public String add(Model model) {
        if (!model.containsAttribute("supplierAddBindingModel")) {
            model.addAttribute("supplierAddBindingModel", new SupplierAddBindingModel());
        }
        if (!model.containsAttribute("addressAddBindingModel")) {
            model.addAttribute("addressAddBindingModel", new AddressAddBindingModel());
        }
        if (!model.containsAttribute("supplierExists")){
            model.addAttribute("supplierExists", false);
        }

        return "suppliers/supplier-add";
    }

    @PostMapping("/add")
    @Validated(OnCreate.class)
    public String addConfirm(@Valid @ModelAttribute("supplierAddBindingModel") SupplierAddBindingModel supplierAddBindingModel,
                             BindingResult supplierBindingResult,
                             @Valid @ModelAttribute("addressAddBindingModel") AddressAddBindingModel addressAddBindingModel,
                             BindingResult addressBindingResult,
                             RedirectAttributes redirectAttributes) {

        if (supplierBindingResult.hasErrors() || addressBindingResult.hasErrors()
                || (this.supplierService.supplierExists(supplierAddBindingModel.getName())) && supplierAddBindingModel.getId() == null) {

            if (this.supplierService.supplierExists(supplierAddBindingModel.getName()) && supplierAddBindingModel.getId() == null){
                redirectAttributes.addFlashAttribute("supplierExists", true);
            }

            redirectAttributes.addFlashAttribute("supplierAddBindingModel", supplierAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.supplierAddBindingModel", supplierBindingResult);

            redirectAttributes.addFlashAttribute("addressAddBindingModel", addressAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressAddBindingModel", addressBindingResult);

            return "redirect:add";
        }

        AddressServiceModel addressServiceModel = this.modelMapper.map(addressAddBindingModel, AddressServiceModel.class);
        SupplierServiceModel supplierServiceModel = this.modelMapper.map(supplierAddBindingModel, SupplierServiceModel.class);
        supplierServiceModel.setAddress(addressServiceModel);

        supplierServiceModel = this.supplierService.add(supplierServiceModel);

        return "redirect:/suppliers/all/pageable";

    }

    @GetMapping("/all/pageable")
    public String getAll(Model model,  @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                         @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         @RequestParam(name = "sortOption", defaultValue = "Name") String sortOption,
                         @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection){

        page = page < 1 ? 0 : page - 1;
        String option = SUPPLIER_SORT_OPTIONS_MAP.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<SupplierServiceModel> supplierServiceModelPage;

        if (!keyword.equals("null")) {
            supplierServiceModelPage = this.supplierService.search(keyword, pageable);
        } else {
            supplierServiceModelPage = this.supplierService.findAllPageable(pageable);
        }

        List<SupplierViewBindingModel> supplierViewBindingModels = supplierServiceModelPage
                .stream()
                .map(supplierServiceModel -> this.modelMapper.map(supplierServiceModel, SupplierViewBindingModel.class))
                .collect(Collectors.toList());

        Page<SupplierViewBindingModel> suppliers = new PageImpl<>(supplierViewBindingModels, pageable, supplierServiceModelPage.getTotalElements());

        PagerModel pager = new PagerModel(suppliers.getTotalPages(), suppliers.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("result", suppliers);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", SUPPLIER_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/suppliers/all/pageable");

        
        return "suppliers/supplier-all";

    }


    @GetMapping("/edit")
    public String edit(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("supplierAddBindingModel")){
            SupplierServiceModel supplierServiceModel = this.supplierService.findById(id);
            SupplierAddBindingModel supplierAddBindingModel = this.modelMapper
                    .map(supplierServiceModel, SupplierAddBindingModel.class);
            model.addAttribute("supplierAddBindingModel", supplierAddBindingModel);
            model.addAttribute("addressAddBindingModel", supplierAddBindingModel.getAddress());
        }

        return "suppliers/supplier-edit";
    }

    @PatchMapping("/edit")
    @Validated(OnUpdate.class)
    public String editConfirm(@Valid @ModelAttribute("supplierAddBindingModel") SupplierAddBindingModel supplierAddBindingModel,
                              BindingResult supplierBindingResult,
                              @Valid @ModelAttribute("addressAddBindingModel") AddressAddBindingModel addressAddBindingModel,
                              BindingResult addressBindingResult,
                              RedirectAttributes redirectAttributes){

        if (supplierBindingResult.hasErrors() || addressBindingResult.hasErrors()){

            redirectAttributes.addFlashAttribute("supplierAddBindingModel", supplierAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.supplierAddBindingModel", supplierBindingResult);

            redirectAttributes.addFlashAttribute("addressAddBindingModel", addressAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addressAddBindingModel", addressBindingResult);

            redirectAttributes.addAttribute("id", supplierAddBindingModel.getId());

            return "redirect:edit";
        }

        SupplierServiceModel supplierServiceModel = this.modelMapper.map(supplierAddBindingModel, SupplierServiceModel.class);
        AddressServiceModel addressServiceModel = this.modelMapper.map(addressAddBindingModel, AddressServiceModel.class);
        supplierServiceModel.setAddress(addressServiceModel);

        supplierServiceModel = this.supplierService.edit(supplierServiceModel);


        return "redirect:/suppliers/all/pageable";
    }

    @PostMapping("/block")
    public String block(@RequestParam(name="blockId") @Min(1) Long id){

        SupplierServiceModel supplierServiceModel = this.supplierService.block(id);

        return "redirect:/suppliers/all/pageable";
    }

    @PostMapping("/unblock")
    public String unblock(@ModelAttribute(name="unblockId") @Min(1) Long id){

        SupplierServiceModel supplierServiceModel = this.supplierService.unblock(id);

        return "redirect:/suppliers/all/pageable";
    }

    @GetMapping("/top")
    public String getTopSuppliers(Model model, @RequestParam(name = "fromDate", defaultValue = "") String fromDate,
                                  @RequestParam(name = "toDate", defaultValue = "") String toDate){

        HashMap<Integer, String> suppliersNamesMap = this.supplierService.getTopSuppliersNamesMap(fromDate, toDate);
        HashMap<Integer, BigDecimal> suppliersTurnoverMap = this.supplierService.getTopSuppliersTurnoverMap(fromDate, toDate);

        model.addAttribute("suppliersNamesMap", suppliersNamesMap);
        model.addAttribute("suppliersTurnoverMap",suppliersTurnoverMap);
        model.addAttribute("path", "/suppliers/top");
        model.addAttribute("fromDate", suppliersNamesMap.get(1));
        model.addAttribute("toDate", suppliersNamesMap.get(2));

        return "suppliers/supplier-pie-chart";
    }

    @GetMapping("/turnover")
    public String getSupplierTurnover(Model model, @RequestParam(name = "fromDate", defaultValue = "") String fromDate,
                                      @RequestParam(name = "toDate", defaultValue = "") String toDate,
                                      @RequestParam(name = "keyword", defaultValue = "null") String keyword){

        String[] timeBorders = this.timeBordersConvertor
                .getTimeBordersAsString(fromDate, toDate, this.orderService.getDateTimeFirstArchiveOrder());
        List<SupplierTurnoverViewModel> suppliers = this.supplierService.getSupplierTurnover(keyword, fromDate, toDate);

        model.addAttribute("path", "/suppliers/turnover");
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("fromDate", timeBorders[0]);
        model.addAttribute("toDate", timeBorders[1]);
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        return "suppliers/supplier-turnover";
    }


}
