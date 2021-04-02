package warehouse.admin;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.model.CategoryViewBindingModel;
import warehouse.pagination.PagerModel;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.users.model.*;
import warehouse.users.service.UserService;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class UserController {

    private static final String[] USER_SORT_OPTIONS = {"Username", "Email", "Department"};

    private final ModelMapper modelMapper;
    private final UserService userService;

    @Autowired
    public UserController(ModelMapper modelMapper, UserService userService) {
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("userRegisterBindingModel")) {
            model.addAttribute("userRegisterBindingModel", new UserRegisterBindingModel());
            model.addAttribute("confirmPasswordCorrect", false);
            model.addAttribute("userExists", false);
        }

        return "users/user-register";
    }

    @PostMapping("/register")
    @Validated(OnCreate.class)
    public String registerConfirm(@Valid @ModelAttribute("userRegisterBindingModel") UserRegisterBindingModel userRegisterBindingModel,
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() ||
                !userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword()) ||
                (this.userService.userExists(userRegisterBindingModel.getUsername()))) {

            if (!userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("confirmPasswordCorrect", true);
            }

            if (this.userService.userExists(userRegisterBindingModel.getUsername())) {
                redirectAttributes.addFlashAttribute("userExists", true);
            }

            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);
            return "redirect:register";
        }

        UserServiceModel userServiceModel = this.modelMapper.map(userRegisterBindingModel, UserServiceModel.class);
        this.userService.register(userServiceModel);

        return "redirect:/users/all/pageable";

    }

    @GetMapping("/all/pageable")
    public String getAll(Model model,
                         @RequestParam(name = "keyword", defaultValue = "null") String keyword,
                         @RequestParam(name = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
                         @RequestParam(name = "page", defaultValue = "0") Integer page,
                         @RequestParam(name = "sortOption", defaultValue = "Username") String sortOption,
                         @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {


        page = page < 1 ? 0 : page - 1;
        String option = sortOption.toLowerCase();

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(option).ascending() :
                Sort.by(option).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<UserServiceModel> userServiceModelPage;

        if (!keyword.equals("null")) {
            userServiceModelPage = this.userService.search(keyword, pageable);
        } else {
            userServiceModelPage = this.userService.findAllPageable(pageable);
        }

        List<UserViewBindingModel> userViewBindingModels = userServiceModelPage
                .stream()
                .map(userServiceModel -> this.modelMapper.map(userServiceModel, UserViewBindingModel.class))
                .collect(Collectors.toList());

        Page<UserViewBindingModel> users = new PageImpl<>(userViewBindingModels, pageable, userServiceModelPage.getTotalElements());

        PagerModel pager = new PagerModel(users.getTotalPages(), users.getNumber(), BUTTONS_TO_SHOW);

        model.addAttribute("users", users);
        model.addAttribute("result", users);
        model.addAttribute("selectedPageSize", pageSize);
        model.addAttribute("pageSizes", PAGE_SIZES);
        model.addAttribute("pager", pager);
        model.addAttribute("selectedSortOption", sortOption);
        model.addAttribute("sortOptions", USER_SORT_OPTIONS);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reversedSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword.equals("null") ? null : keyword);
        model.addAttribute("path", "/users/all/pageable");

        return "users/user-all";
    }



    @GetMapping("/edit")
    public String edit(Model model, @RequestParam("id") @Min(1) Long id) {

        if (!model.containsAttribute("userRegisterBindingModel")) {
            UserServiceModel userServiceModel = this.userService.findUserById(id);
            UserRegisterBindingModel userRegisterBindingModel = this.modelMapper.map(userServiceModel, UserRegisterBindingModel.class);
            model.addAttribute("userRegisterBindingModel", userRegisterBindingModel);
            model.addAttribute("confirmPasswordCorrect", false);
        }


        return "users/user-edit";
    }

    @PatchMapping("/edit")
    @Validated(OnUpdate.class)
    public String editConfirm(@Valid @ModelAttribute("userRegisterBindingModel") UserRegisterBindingModel userRegisterBindingModel,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || !userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);
            redirectAttributes.addAttribute("id", userRegisterBindingModel.getId());

            if (!userRegisterBindingModel.getPassword().equals(userRegisterBindingModel.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("confirmPasswordCorrect", true);
            }

            return "redirect:edit";
        }

        this.userService.edit(this.modelMapper.map(userRegisterBindingModel, UserServiceModel.class));


        return "redirect:/users/all/pageable";
    }

    @PostMapping("/block")
    public String block(@RequestParam(name="blockId") @Min(1) Long id){

        UserServiceModel userServiceModel = this.userService.block(id);

        return "redirect:/users/all/pageable";
    }

    @PostMapping("/unblock")
    public String unblock(@ModelAttribute(name="unblockId") @Min(1) Long id){

        UserServiceModel userServiceModel = this.userService.unblock(id);

        return "redirect:/users/all/pageable";
    }

    @GetMapping("/roles/addRole")
    @PreAuthorize("hasRole('ADMIN')")
    public String addRole(Model model, @RequestParam("id") @Min(1) Long id) {

        UserServiceModel userServiceModel = this.userService.findUserById(id);
        UserViewBindingModel userViewBindingModel = this.modelMapper.map(userServiceModel, UserViewBindingModel.class);
        model.addAttribute("user", userViewBindingModel);

        Set<String> userRoles = this.userService.getRolesByUserId(id);
        model.addAttribute("userRoles", userRoles);

        if (!model.containsAttribute("roleAddBindingModel")){
            model.addAttribute("roleAddBindingModel", new RoleAddBindingModel());
        }

        return "users/user-role-add";
    }

    @PostMapping("/roles/addRole")
    public String addRoleConfirm(@Valid @ModelAttribute("roleAddBindingModel") RoleAddBindingModel roleAddBindingModel,
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addAttribute("id", roleAddBindingModel.getUserId());
            redirectAttributes.addFlashAttribute("roleAddBindingModel", roleAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.roleAddBindingModel", bindingResult);
            return "redirect:addRole";
        }
        this.userService.addRole(roleAddBindingModel.getUserId(), roleAddBindingModel.getRole());

        return "redirect:/users/all/pageable";
    }

    @GetMapping("/roles/remove")
    public String removeRole(Model model, @RequestParam("id") @Min(1) Long id) {

        UserServiceModel userServiceModel = this.userService.findUserById(id);
        UserViewBindingModel userViewBindingModel = this.modelMapper.map(userServiceModel, UserViewBindingModel.class);
        model.addAttribute("user", userViewBindingModel);

        Set<String> userRoles = this.userService.getRolesByUserId(id);
        model.addAttribute("userRoles", userRoles);

        if (!model.containsAttribute("roleAddBindingModel")){
            model.addAttribute("roleAddBindingModel", new RoleAddBindingModel());
        }

        return "users/user-role-remove";
    }

    @PostMapping("/roles/remove")
    public String removeRoleConfirm(@Valid @ModelAttribute("roleAddBindingModel") RoleAddBindingModel roleAddBindingModel,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addAttribute("id", roleAddBindingModel.getUserId());
            redirectAttributes.addFlashAttribute("roleAddBindingModel", roleAddBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.roleAddBindingModel", bindingResult);
            return "redirect:remove";
        }

        this.userService.removeRole(roleAddBindingModel.getUserId(), roleAddBindingModel.getRole());

        return "redirect:/users/all/pageable";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public String profileEdit(Model model, @RequestParam("name") String name) {
        if (!model.containsAttribute("userRegisterBindingModel")) {
            UserServiceModel userServiceModel = this.userService.getUserByUserName(name);
            UserRegisterBindingModel userRegisterBindingModel = this.modelMapper.map(userServiceModel, UserRegisterBindingModel.class);
            model.addAttribute("userRegisterBindingModel", userRegisterBindingModel);
            model.addAttribute("confirmPasswordCorrect", false);
        }

        return "users/user-profile-edit";
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Validated(OnUpdate.class)
    public String profileEditConfirm(@Valid @ModelAttribute("userRegisterBindingModel") UserRegisterBindingModel userRegisterBindingModel,
                                     BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || !userRegisterBindingModel.getConfirmPassword().equals(userRegisterBindingModel.getPassword())) {
            redirectAttributes.addFlashAttribute("userRegisterBindingModel", userRegisterBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterBindingModel", bindingResult);
            redirectAttributes.addAttribute("name", userRegisterBindingModel.getUsername());

            if (!userRegisterBindingModel.getConfirmPassword().equals(userRegisterBindingModel.getPassword())) {
                redirectAttributes.addFlashAttribute("confirmPasswordCorrect", true);
            }
            return "redirect:profile";

        }

        this.userService.edit(this.modelMapper.map(userRegisterBindingModel, UserServiceModel.class));

        return "redirect:/";
    }

}
