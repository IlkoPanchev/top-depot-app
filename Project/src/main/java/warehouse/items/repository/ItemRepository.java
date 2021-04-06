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

    @Query("select i.name as name, sum(ol.quantity) as quantity, sum(ol.subtotal) as turnover from ItemEntity as i" +
            " join OrderLineEntity as ol" +
            " on i.id = ol.item.id" +
            " join OrderEntity as o" +
            " on ol.order.id = o.id" +
            " where o.updatedOn between :fromDate and :toDate" +
            " and o.archives = true group by ol.item.id order by quantity desc , i.name asc")
    List<Object[]> findTopItems(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);



}
