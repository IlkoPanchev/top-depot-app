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
import warehouse.categories.model.CategoryAddBindingModel;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.model.CategoryViewBindingModel;
import warehouse.categories.service.CategoryService;
import warehouse.pagination.PagerModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;


@Controller
@RequestMapping("/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Validated
public class CategoryController {

    private static final String[] CATEGORY_SORT_OPTIONS = {"Name", "Description"};

    private final CategoryService categoryService;

    private final ModelMapper modelMapper;

    @Autowired
    public CategoryController(CategoryService categoryService,  ModelMapper modelMapper) {
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/all/pageable")
    public String findAllPageable(Model model,
                                  @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                                  @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                                  @RequestParam(name = "page", defaultValue = "0") Integer page,
                                  @RequestParam(name = "sortOption", defaultValue = "Name") String sortOption,
                                  @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {

        page = page < 1 ? 0 : page - 1;
        String option = sortOption.toLowerCase();

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<CategoryServiceModel> categoryServiceModelPage;

        if (!keyword.equals("null")) {
            categoryServiceModelPage = this.categoryService.search(keyword, pageable);
        } else {
            categoryServiceModelPage = this.categoryService.findAllPageable(pageable);
        }

        List<CategoryViewBindingModel> categoryViewBindingModels = categoryServiceModelPage
                .stream()
                .map(categoryServiceModel -> this.modelMapper.map(categoryServiceModel, CategoryViewBindingModel.class))
                .collect(Collectors.toList());

        Page<CategoryViewBindingModel> categories = new PageImpl<>(categoryViewBindingModels, pageable, categoryServiceModelPage.getTotalElements());

        PagerModel pager = new PagerModel(categories.getTotalPages(), categories.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("categories", categories);
        model.addAttribute("result", categories);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", CATEGORY_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/categories/all/pageable");

        return "categories/category-all";
    }

    @GetMapping("/add")
    public String add(Model model) {

        if (!model.containsAttribute("categoryAddBindingModel")) {
            model.addAttribute("categoryAddBindingModel", new CategoryAddBindingModel());
            model.addAttribute("categoryExists", false);
        }

        return "categories/category-add";
    }

    @PostMapping("/add")
    @Validated(OnCreate.class)
    public String addConfirm(@Valid @ModelAttribute("categoryAddBindingModel") CategoryAddBindingModel categoryAddBindingModel,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || (this.categoryService.categoryExistsByName(categoryAddBindingModel.getName()) && categoryAddBindingModel.getId() == null)) {

            if (this.categoryService.categoryExistsByName(categoryAddBindingModel.getName()) && categoryAddBindingModel.getId() == null) {
                redirectAttributes.addFlashAttribute("categoryExists", true);
            }
            redirectAttributes.addFlashAttribute("categoryAddBindingModel", categoryAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryAddBindingModel", bindingResult);

            return "redirect:add";
        }

        CategoryServiceModel categoryServiceModel = this.categoryService
                .add(this.modelMapper.map(categoryAddBindingModel, CategoryServiceModel.class));

        return "redirect:/categories/all/pageable";
    }

    @GetMapping("/edit")
    public String edit(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("categoryAddBindingModel")) {
            CategoryAddBindingModel categoryAddBindingModel = this.modelMapper
                    .map(this.categoryService.findById(id), CategoryAddBindingModel.class);
            model.addAttribute("categoryAddBindingModel", categoryAddBindingModel);
        }
        return "categories/category-edit";
    }

    @PatchMapping("/edit")
    @Validated(OnUpdate.class)
    public String editConfirm(@Valid @ModelAttribute("categoryAddBindingModel") CategoryAddBindingModel categoryAddBindingModel,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || (this.categoryService.categoryExistsByName(categoryAddBindingModel.getName()) && categoryAddBindingModel.getId() == null)) {

            if (this.categoryService.categoryExistsByName(categoryAddBindingModel.getName()) && categoryAddBindingModel.getId() == null) {
                redirectAttributes.addFlashAttribute("categoryExists", true);
            }
            redirectAttributes.addFlashAttribute("categoryAddBindingModel", categoryAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryAddBindingModel", bindingResult);

            redirectAttributes.addAttribute("id", categoryAddBindingModel.getId());

            return "redirect:edit";
        }

        CategoryServiceModel categoryServiceModel = (this.modelMapper.map(categoryAddBindingModel, CategoryServiceModel.class));

         categoryServiceModel = this.categoryService.edit(categoryServiceModel);

        return "redirect:/categories/all/pageable";
    }

    @PostMapping("/block")
    public String remove(@ModelAttribute(name="blockId") @Min(1) Long id){

        CategoryServiceModel categoryServiceModel = this.categoryService.block(id);

        return "redirect:/categories/all/pageable";
    }

    @PostMapping("/unblock")
    public String restore(@ModelAttribute(name="unblockId") @Min(1) Long id){

        CategoryServiceModel categoryServiceModel = this.categoryService.unblock(id);

        return "redirect:/categories/all/pageable";
    }

}
