package warehouse.orders.orderdata.service;

import org.springframework.stereotype.Service;
import warehouse.orders.orderdata.OrderData;

import javax.servlet.http.HttpSession;

@Service
public class OrderDataManager {

    public OrderData getOrderData(HttpSession session){

        OrderData orderData = (OrderData) session.getAttribute("orderData");

        if(orderData == null){
            orderData = new OrderData();
            setOrderData(session, orderData);
        }

        return orderData;
    }



    public void setOrderData(HttpSession session, OrderData orderData){
        session.setAttribute("orderData", orderData);
    }

    public void removeOrderData(HttpSession session){
        session.removeAttribute("orderData");
    }



    public OrderData getEditOrderData(HttpSession session){

        OrderData orderData = (OrderData) session.getAttribute("editOrderData");

        if(orderData == null){
            orderData = new OrderData();
            setEditOrderData(session, orderData);
        }

        return orderData;
    }

    public void setEditOrderData(HttpSession session, OrderData orderData){
        session.setAttribute("editOrderData", orderData);
    }

    public void removeEditOrderData(HttpSession session){
        session.removeAttribute("editOrderData");
    }
}
