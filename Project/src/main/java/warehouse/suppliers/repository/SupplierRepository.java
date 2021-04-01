package warehouse.suppliers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import warehouse.items.model.ItemEntity;
import warehouse.suppliers.model.SupplierEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> {
    Optional<SupplierEntity> findByName(String name);

    @Query("SELECT s FROM SupplierEntity s WHERE CONCAT(lower(s.name), lower(s.email), lower(s.addressEntity.region)," +
            " lower(s.addressEntity.city), lower(s.addressEntity.street), lower(s.addressEntity.phone)) LIKE lower(concat('%', ?1,'%'))")
    Page<SupplierEntity> search(String keyword, Pageable pageable);

    @Query(value = "SELECT s.name AS 'name', sum(ol.subtotal) AS 'turnover' FROM suppliers AS s\n" +
            "            JOIN items AS i\n" +
            "            ON s.id = i.supplier_id\n" +
            "            JOIN order_lines AS ol\n" +
            "            ON i.id = ol.item_id\n" +
            "            JOIN orders_order_lines AS ool\n" +
            "            ON ol.id = ool.order_line_id\n" +
            "            JOIN orders AS o\n" +
            "            ON ool.order_id = o.id\n" +
            "\t\t\tWHERE o.updated_on BETWEEN :fromDate AND :toDate\n" +
            "            AND o.is_archives = TRUE\n" +
            "            GROUP BY s.id\n" +
            "            ORDER BY turnover DESC, s.name\n" +
            "            LIMIT 3;", nativeQuery = true)
    List<Object[]> findTopSuppliers(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT s.name AS 'name', sum(ol.subtotal) AS 'turnover' , sum(ol.quantity) AS 'quantity' FROM suppliers AS s\n" +
            "                       JOIN addresses AS a\n" +
            "                        ON s.address_id = a.id\n" +
            "                        JOIN items AS i\n" +
            "                        ON s.id = i.supplier_id\n" +
            "                        JOIN order_lines AS ol\n" +
            "                       ON i.id = ol.item_id\n" +
            "                        JOIN orders_order_lines AS ool\n" +
            "                        ON ol.id = ool.order_line_id\n" +
            "                        JOIN orders AS o\n" +
            "                        ON ool.order_id = o.id\n" +
            "             WHERE o.updated_on BETWEEN :fromDate AND :toDate\n" +
            "            AND CONCAT(lower(s.name), lower(s.email), lower(a.region),\n" +
            "            lower(a.city), lower(a.street), lower(a.phone)) LIKE lower(concat('%', :keyword,'%'))\n" +
            "                        AND o.is_archives = TRUE\n" +
            "                        GROUP BY s.id\n" +
            "                        ORDER BY s.name, turnover DESC\n" +
            "                        LIMIT 5;", nativeQuery = true)
    List<Object[]> findSupplierTurnover(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("keyword") String keyword);

    @Query("select s.name from SupplierEntity AS s")
    List<String> findAllSupplierNames();
}
