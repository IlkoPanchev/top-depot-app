package warehouse.customers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import warehouse.customers.model.CustomerEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByCompanyName(String companyName);

    @Query("SELECT c FROM CustomerEntity c WHERE CONCAT(lower(c.companyName), lower(c.personName), lower(c.email), lower(c.addressEntity.region)," +
            " lower(c.addressEntity.city), lower(c.addressEntity.street), lower(c.addressEntity.phone)) LIKE lower(concat('%', ?1,'%'))")
    Page<CustomerEntity> search(String keyword, Pageable pageable);


    Page<CustomerEntity> findAllByBlockedFalse(Pageable pageable);

    @Query("SELECT c FROM CustomerEntity c WHERE c.blocked = false AND CONCAT(lower(c.companyName), lower(c.personName), lower(c.email), lower(c.addressEntity.region)," +
            " lower(c.addressEntity.city), lower(c.addressEntity.street), lower(c.addressEntity.phone)) LIKE lower(concat('%', ?1,'%'))")
    Page<CustomerEntity> searchUnblocked(String keyword, Pageable pageable);

    @Query(value = "SELECT c.company_name AS 'company_name', c.person_name AS 'person_name', sum(ol.subtotal) AS 'turnover'," +
            " count(distinct o.id) AS 'orders_count', sum(ol.quantity) AS 'items_count' FROM customers AS c\n" +
            "                                   JOIN addresses AS a\n" +
            "                                    ON c.address_id = a.id\n" +
            "                                    JOIN orders AS o\n" +
            "                                    ON c.id = o.customer_id\n" +
            "                                    JOIN orders_order_lines AS ool\n" +
            "                                   ON o.id = ool.order_id\n" +
            "                                    JOIN order_lines AS ol\n" +
            "                                    ON ol.id = ool.order_line_id\n" +
            "                                    JOIN items AS i\n" +
            "                                    ON ol.item_id = i.id\n" +
            "                         WHERE o.updated_on BETWEEN :fromDate AND :toDate\n" +
            "                        AND CONCAT(lower(c.company_name), lower(c.person_name), lower(c.email), lower(a.region),\n" +
            "                        lower(a.city), lower(a.street), lower(a.phone)) LIKE lower(concat('%', :keyword,'%'))\n" +
            "                                    AND o.is_archives = TRUE\n" +
            "                                    GROUP BY c.id\n" +
            "                                    ORDER BY c.company_name, c.person_name, turnover DESC\n" +
            "                                    LIMIT 5;", nativeQuery = true)
    List<Object[]> findCustomerTurnover(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("keyword") String keyword);
}
