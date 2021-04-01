package warehouse.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import warehouse.addresses.service.AddressService;
import warehouse.categories.service.CategoryService;
import warehouse.customers.service.CustomerService;
import warehouse.departments.service.DepartmentService;
import warehouse.items.service.ItemService;
import warehouse.orderline.service.OrderLineService;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.service.SupplierService;
import warehouse.users.service.UserService;

@Component
public class AppInit implements CommandLineRunner {


    private final DepartmentService departmentService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final AddressService addressService;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final OrderLineService orderLineService;


    @Autowired
    public AppInit(DepartmentService departmentService,
                   CategoryService categoryService,
                   UserService userService,
                   AddressService addressService,
                   CustomerService customerService,
                   SupplierService supplierService,
                   ItemService itemService,
                   OrderService orderService,
                   OrderLineService orderLineService) {

        this.departmentService = departmentService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.addressService = addressService;
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.orderLineService = orderLineService;
    }


    @Override
    public void run(String... args) throws Exception {

        this.departmentService.initDepartments();

        this.categoryService.initCategories();

        this.userService.initUsers();

        this.addressService.initAddresses();

        this.customerService.initCustomers();

        this.supplierService.initSuppliers();

        this.itemService.initItems();

        this.orderLineService.initOrderLines();

        this.orderService.initOrders();


    }

}
