package warehouse.orders.aspect;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import warehouse.orders.orderdata.OrderData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Component
@Aspect
public class TrackOrderAspect {


    private static Logger logger = Logger.getLogger(TrackOrderAspect.class);


    @Pointcut("execution(* warehouse.manager.OrderController.save(..))")
    private void save(){}


    @After("save()")
    public void afterSave(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order created by %s", username));
    }

    @Pointcut("execution(* warehouse.manager.OrderController.editOrderSave(..))")
    private void editSave(){}

    @Before("editSave()")
    public void beforeEditSave(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        Long orderId = getOrderId(joinPoint);
        logger.info(String.format("request to edit order %s by %s",orderId, username));
    }

    @After("editSave()")
    public void afterEditSave(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order edited by %s", username));
    }

    @Pointcut("execution(* warehouse.manager.OrderController.editOrderComplete(..))")
    private void editComplete(){}

    @Before("editComplete()")
    public void beforeEditOrderComplete(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        Long orderId = getOrderId(joinPoint);
        logger.info(String.format("request to complete order %s by %s",orderId, username));
    }

    @After("editComplete()")
    public void afterEditOrderComplete(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order completed by %s", username));
    }


    @Pointcut("execution(* warehouse.manager.OrderController.orderComplete(..))")
    private void orderComplete(){}

    @Before("orderComplete()")
    public void beforeOrderComplete(JoinPoint joinPoint) {
        String username = getUsername(joinPoint);
        Long orderId = getOrderId(joinPoint);
        logger.info(String.format("request to complete order %s by %s",orderId, username));
    }



    @After("orderComplete()")
    public void afterOrderComplete(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order completed by %s", username));
    }



    @Pointcut("execution(* warehouse.manager.OrderController.orderIncomplete(..))")
    private void orderIncomplete(){}

    @Before("orderIncomplete()")
    public void beforeOrderIncomplete(JoinPoint joinPoint) {
        String username = getUsername(joinPoint);
        Long orderId = getOrderId(joinPoint);
        logger.info(String.format("request to incomplete order %s by %s",orderId, username));
    }

    @After("orderIncomplete()")
    public void afterOrderIncomplete(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order incomplete by %s", username));
    }

    @Pointcut("execution(* warehouse.manager.OrderController.orderArchive(..))")
    private void orderArchive(){}

    @Before("orderArchive()")
    public void beforeOrderArchive(JoinPoint joinPoint) {
        String username = getUsername(joinPoint);
        Long orderId = getOrderId(joinPoint);
        logger.info(String.format("request to archive order %s by %s",orderId, username));
    }

    @After("orderArchive()")
    public void afterOrderArchive(JoinPoint joinPoint){
        String username = getUsername(joinPoint);
        logger.info(String.format("order archived by %s", username));
    }

    private String getUsername(JoinPoint joinPoint) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) joinPoint.getArgs()[1];
        String username = httpServletRequest.getUserPrincipal().getName();
        return username;
    }

    private Long getOrderId(JoinPoint joinPoint) {
        HttpSession httpSession = (HttpSession) joinPoint.getArgs()[0];
        OrderData orderData = (OrderData) httpSession.getAttribute("editOrderData");
        Long orderId = orderData.getId();
        return orderId;
    }
}
