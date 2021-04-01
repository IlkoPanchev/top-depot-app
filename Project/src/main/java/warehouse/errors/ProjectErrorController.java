package warehouse.errors;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ProjectErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = Integer.parseInt(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "errors/error-404";
            }
            else if (statusCode == HttpStatus.FORBIDDEN.value()){
                return "errors/error-403";
            }
            else if (statusCode == HttpStatus.UNAUTHORIZED.value()){
                return "errors/error-401";
            }
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()){
                return "errors/error-500";
            }

       return "errors/error";

    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
