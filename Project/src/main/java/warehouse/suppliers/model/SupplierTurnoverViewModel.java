package warehouse.suppliers.model;

import java.math.BigDecimal;

public class SupplierTurnoverViewModel {
    private String name;
    private BigDecimal turnover;
    private int soldItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public int getSoldItems() {
        return soldItems;
    }

    public void setSoldItems(int soldItems) {
        this.soldItems = soldItems;
    }
}
