package warehouse.statistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class StatsController {

    private final StatsService statsService;
    private final UnauthorizedErrorService unauthorizedErrorService;

    @Autowired
    public StatsController(StatsService statsService, UnauthorizedErrorService unauthorizedErrorService) {
        this.statsService = statsService;
        this.unauthorizedErrorService = unauthorizedErrorService;
    }

    @GetMapping("/stats")
    public String stats(Model model) {

        model.addAttribute("requestsCount", statsService.getRequestsCount());
        model.addAttribute("errorsCount", statsService.getErrorsCount());

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String startedOn = dateTimeFormatter.format(this.statsService.getStartedOn());
        model.addAttribute("startedOn", startedOn);

        return "stats/requests-stats";
    }

    @GetMapping("/unauthorized")
    public String getUnauthorizedErrors(Model model){

        model.addAttribute("unauthorizedErrors", unauthorizedErrorService.getUnauthorizedErrorList());

        return "stats/unauthorized-errors";
    }

}
