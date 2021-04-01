package warehouse.customers.model;

import java.math.BigDecimal;

public class CustomerTurnoverViewModel {
    private String companyName;
    private String personName;
    private BigDecimal turnover;
    private int ordersCount;
    private int orderedItems;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public int getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(int orderedItems) {
        this.orderedItems = orderedItems;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }
}
