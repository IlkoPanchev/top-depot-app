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
import warehouse.categories.service.CategoryService;
import warehouse.items.model.*;
import warehouse.items.service.ItemService;
import warehouse.pagination.PagerModel;
import warehouse.suppliers.service.SupplierService;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Controller
@RequestMapping("/items")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@Validated
public class ItemController {

    private static final String[] ITEM_SORT_OPTIONS = {"Name", "Description", "Price", "Location", "Category", "Supplier"};
    private static final Map<String, String> ITEM_SORT_OPTIONS_MAP = Map.of("Name","name",
            "Description", "description",
            "Price", "price",
            "Location", "location",
            "Category", "category.name",
            "Supplier", "supplier.name");

    private final ModelMapper modelMapper;
    private final ItemService itemService;
    private final SupplierService supplierService;
    private final CategoryService categoryService;

    @Autowired
    public ItemController(ModelMapper modelMapper,
                          ItemService itemService,
                          SupplierService supplierService,
                          CategoryService categoryService) {
        this.modelMapper = modelMapper;
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.categoryService = categoryService;
    }


    @GetMapping("/add")
    public String add(Model model) {
        if (!model.containsAttribute("itemAddBindingModel")) {
            model.addAttribute("itemAddBindingModel", new ItemAddBindingModel());
            model.addAttribute("suppliers", this.supplierService.findAll());
            model.addAttribute("categories", this.categoryService.findAll());
            model.addAttribute("itemExists", false);
        }
        return "items/item-add";
    }

    @PostMapping("/add")
    @Validated(OnCreate.class)
    public String addConfirm(@Valid @ModelAttribute("itemAddBindingModel") ItemAddBindingModel itemAddBindingModel,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors() || (this.itemService.itemExists(itemAddBindingModel.getName())) && itemAddBindingModel.getId() == null) {

            if (this.itemService.itemExists(itemAddBindingModel.getName()) && itemAddBindingModel.getId() == null){
                redirectAttributes.addFlashAttribute("itemExists", true);
            }

            redirectAttributes.addFlashAttribute("itemAddBindingModel", itemAddBindingModel);
            redirectAttributes.addFlashAttribute("suppliers", this.supplierService.findAll());
            redirectAttributes.addFlashAttribute("categories", this.categoryService.findAll());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.itemAddBindingModel", bindingResult);
            return "redirect:add";
        }

        ItemAddServiceModel itemAddServiceModel = this.itemService.add(this.modelMapper.map(itemAddBindingModel, ItemAddServiceModel.class));

        return "redirect:/items/all/pageable";

    }

    @GetMapping("/all/pageable")
    public String getAll(Model model,
                         @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                         @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         @RequestParam(name = "sortOption", defaultValue = "Name") String sortOption,
                         @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {

        page = getCurrentPageNumber(page);

        Page<ItemViewBindingModel> items = this.getPage(ITEM_SORT_OPTIONS_MAP, sortOption, sortDirection, keyword, page, pageSize);

        PagerModel pager = new PagerModel(items.getTotalPages(), items.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("items", items);
        model.addAttribute("result", items);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", ITEM_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/items/all/pageable");

        return "items/item-all";
    }



    @GetMapping("/all/pageable/editOrder")
    public String getAllEditOrder(Model model,   @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                                  @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                                  @RequestParam(name = "page", defaultValue = "0") Integer page,
                                  @RequestParam(name = "sortOption", defaultValue = "Name") String sortOption,
                                  @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {


        page = getCurrentPageNumber(page);

        Page<ItemViewBindingModel> items = this.getPageUnblocked(ITEM_SORT_OPTIONS_MAP, sortOption, sortDirection, keyword, page, pageSize);

        PagerModel pager = new PagerModel(items.getTotalPages(), items.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("items", items);
        model.addAttribute("result", items);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", ITEM_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/items/all/pageable/editOrder");

        return "items/item-all-order-edit";
    }

    @GetMapping("/all/pageable/orderAddItem")
    public String getAllOrderAddItem(Model model,   @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                                     @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                                     @RequestParam(name = "page", defaultValue = "0") Integer page,
                                     @RequestParam(name = "sortOption", defaultValue = "Name") String sortOption,
                                     @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {

        page = getCurrentPageNumber(page);

        Page<ItemViewBindingModel> items = this.getPageUnblocked(ITEM_SORT_OPTIONS_MAP, sortOption, sortDirection, keyword, page, pageSize);

        PagerModel pager = new PagerModel(items.getTotalPages(), items.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("items", items);
        model.addAttribute("result", items);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", ITEM_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/items/all/pageable/orderAddItem");

        return "items/item-all-order-add-item";
    }


    @GetMapping("/edit")
    public String edit(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("itemAddBindingModel")) {
            ItemAddBindingModel itemAddBindingModel = this.modelMapper
                    .map(this.itemService.findById(id), ItemAddBindingModel.class);
            model.addAttribute("itemAddBindingModel", itemAddBindingModel);
            model.addAttribute("suppliers", this.supplierService.findAll());
            model.addAttribute("categories", this.categoryService.findAll());
        }

        return "items/item-edit";
    }



    @PatchMapping("/edit")
    @Validated(OnUpdate.class)
    public String editConfirm(@Valid @ModelAttribute("itemAddBindingModel") ItemAddBindingModel itemAddBindingModel,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("itemAddBindingModel", itemAddBindingModel);
            redirectAttributes.addFlashAttribute("suppliers", this.supplierService.findAll());
            redirectAttributes.addFlashAttribute("categories", this.categoryService.findAll());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.itemAddBindingModel", bindingResult);
            redirectAttributes.addAttribute("id", itemAddBindingModel.getId());

            return "redirect:edit";

        }

        ItemAddServiceModel itemAddServiceModel = this.itemService.edit(this.modelMapper.map(itemAddBindingModel, ItemAddServiceModel.class));

        return "redirect:/items/all/pageable";
    }


    @PostMapping("/block")
    public String block(@ModelAttribute(name="blockId") @Min(1) Long id){

        ItemViewServiceModel itemViewServiceModel = this.itemService.block(id);

        return "redirect:/items/all/pageable";
    }

    @PostMapping("/unblock")
    public String unblock(@ModelAttribute(name="unblockId") @Min(1) Long id){

        ItemViewServiceModel itemViewServiceModel = this.itemService.unblock(id);

        return "redirect:/items/all/pageable";
    }

    @GetMapping("/top")
    public String topItems(Model model, @RequestParam(name = "fromDate", defaultValue = "") String fromDate,
                           @RequestParam(name = "toDate", defaultValue = "") String toDate){

        HashMap<Integer, String> itemsNamesMap = this.itemService.getTopItemsNamesMap(fromDate, toDate);
        HashMap<Integer, Integer> itemsQuantityMap = this.itemService.getTopItemsQuantityMap(fromDate, toDate);
        HashMap<Integer, BigDecimal> itemsTurnoverMap = this.itemService.getTopItemsTurnoverMap(fromDate, toDate);
        model.addAttribute("path", "/items/top");
        model.addAttribute("itemsNamesMap", itemsNamesMap);
        model.addAttribute("itemsQuantityMap", itemsQuantityMap);
        model.addAttribute("itemsTurnoverMap", itemsTurnoverMap);
        model.addAttribute("fromDate", itemsNamesMap.get(1));
        model.addAttribute("toDate", itemsNamesMap.get(2));

        return "items/items-pie-chart";
    }


    private Page<ItemViewBindingModel> getPage(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize) {

        String option = sortOptions.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<ItemViewServiceModel> itemViewServiceModelPage;

        if (!keyword.equals("null")) {
            itemViewServiceModelPage = this.itemService.search(keyword, pageable);
        } else {
            itemViewServiceModelPage = this.itemService.findAllPageable(pageable);
        }

        List<ItemViewBindingModel> itemViewBindingModels = itemViewServiceModelPage
                .stream()
                .map(itemViewServiceModel -> this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class))
                .collect(Collectors.toList());

        Page<ItemViewBindingModel> pageToReturn = new PageImpl<>(itemViewBindingModels, pageable, itemViewServiceModelPage.getTotalElements());

        return pageToReturn;
    }

    private Page<ItemViewBindingModel> getPageUnblocked(Map<String, String> sortOptions, String sortOption, String sortDirection, String keyword, Integer page, Integer pageSize) {

        String option = sortOptions.get(sortOption);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<ItemViewServiceModel> itemViewServiceModelPage;

        if (!keyword.equals("null")) {
            itemViewServiceModelPage = this.itemService.searchUnblocked(keyword, pageable);
        } else {
            itemViewServiceModelPage = this.itemService.findAllPageableUnblocked(pageable);
        }

        List<ItemViewBindingModel> itemViewBindingModels = itemViewServiceModelPage
                .stream()
                .map(itemViewServiceModel -> this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class))
                .collect(Collectors.toList());

        Page<ItemViewBindingModel> pageToReturn = new PageImpl<>(itemViewBindingModels, pageable, itemViewServiceModelPage.getTotalElements());

        return pageToReturn;
    }

    private Integer getCurrentPageNumber(Integer page) {
        return   page < 1 ? 0 : page - 1;
    }

}
