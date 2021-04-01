package warehouse.constants;

import warehouse.categories.model.CategoryEntity;
import warehouse.suppliers.model.SupplierEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GlobalConstants {

    public  static  final String EXPORT_FILE_PATH = "src/main/resources/export/";

    public static final int BUTTONS_TO_SHOW = 3;

    public static final String DEFAULT_PAGE_SIZE = "5";

    public static final int[] PAGE_SIZES = {5, 10, 15, 20};

    public static final int INIT_COUNT = 7;

    public static final Map<String, String> CATEGORY_NAMES  = new TreeMap<String, String>() {{
        put("laptops", "description laptops");
        put("tablets", "description tablets");
        put("mobile", "description mobile");
        put("printers", "description printers");
        put("desctop", "description desctop");
        put("scanners", "description scanners");
    }};

}
