package warehouse.items.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.users.model.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long>, JpaSpecificationExecutor<ItemEntity> {

    Optional<ItemEntity> findByName(String name);

    Page<ItemEntity> findAllByBlockedFalse(Pageable pageable);

    @Query("SELECT i FROM ItemEntity i WHERE CONCAT(lower(i.name), lower(i.description), lower(i.price), lower(i.location)," +
            " lower(i.category.name), lower(i.supplier.name)) LIKE lower(concat('%', ?1, '%'))")
    Page<ItemEntity> search(String keyword, Pageable pageable);

    @Query("SELECT i FROM ItemEntity i WHERE i.blocked = false AND CONCAT(lower(i.name), lower(i.description), lower(i.price), lower(i.location)," +
            " lower(i.category.name), lower(i.supplier.name)) LIKE lower(concat('%', ?1, '%'))")
    Page<ItemEntity> searchUnblocked(String keyword, Pageable pageable);

    @Query(value = "SELECT i.name AS 'name',  sum(ol.quantity) AS 'quantity', sum(ol.subtotal) AS 'turnover' FROM items AS i\n" +
            "            JOIN order_lines AS ol\n" +
            "            ON i.id = ol.item_id\n" +
            "            JOIN orders_order_lines AS ool\n" +
            "            ON ol.id = ool.order_line_id\n" +
            "            JOIN orders AS o\n" +
            "            ON ool.order_id = o.id\n" +
            "            WHERE o.updated_on BETWEEN :fromDate AND :toDate\n" +
            "            AND o.is_archives = TRUE\n" +
            "            GROUP BY ol.item_id\n" +
            "            ORDER BY quantity DESC, i.name\n" +
            "            LIMIT 3;", nativeQuery = true)
    List<Object[]> findTopItems(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);



}
