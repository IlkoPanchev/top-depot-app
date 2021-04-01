package warehouse.home;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import warehouse.orders.model.OrderViewBindingModel;
import warehouse.orders.service.OrderService;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final OrderService orderService;
    private final ModelMapper modelMapper;

    @Autowired
    public HomeController(OrderService orderService, ModelMapper modelMapper) {
        this.orderService = orderService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/")
    public String home(Model model) {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

            List<OrderViewBindingModel> lastCompletedOrders = this.orderService.findAllPageableCompletedOrderByUpdated()
                    .stream()
                    .map(orderViewServiceModel -> this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class))
                    .collect(Collectors.toList());
            model.addAttribute("lastCompletedOrders", lastCompletedOrders);


        List<OrderViewBindingModel> lastCreatedOrders = this.orderService.findAllPageableOrderByCreated()
                .stream()
                .map(orderViewServiceModel -> this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class))
                .collect(Collectors.toList());
        model.addAttribute("lastCreatedOrders", lastCreatedOrders);


        List<OrderViewBindingModel> lastUpdatedOrders = this.orderService.findAllPageableOrderByUpdated()
                .stream()
                .map(orderViewServiceModel -> this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class))
                .collect(Collectors.toList());
        model.addAttribute("lastUpdatedOrders", lastUpdatedOrders);

        model.addAttribute("totalOrders", this.orderService.getRepositoryCount());


        return "home";
    }

    @GetMapping("/home")
    public String homeAbsolute(Model model) {

        return home(model);
    }

    @PostMapping("/home")
    public String homePost() {

        return "redirect:/home";
    }
}