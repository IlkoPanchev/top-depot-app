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

    @Query("select s.name as name, sum(ol.subtotal) as turnover from SupplierEntity as s" +
            " join ItemEntity as i" +
            " on s.id = i.supplier.id" +
            " join OrderLineEntity as ol" +
            " on i.id = ol.item.id" +
            " join OrderEntity as o" +
            " on ol.order.id = o.id" +
            " where o.updatedOn between :fromDate and :toDate" +
            " and o.archives = true" +
            " group by s.id" +
            " order by turnover desc, s.name")
    List<Object[]> findTopSuppliers(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);

    @Query("select s.name as name, sum(ol.subtotal) as turnover, sum(ol.quantity) as quantity from SupplierEntity as s" +
            " join AddressEntity as a" +
            " on s.addressEntity.id = a.id" +
            " join ItemEntity as i" +
            " on s.id = i.supplier.id" +
            " join OrderLineEntity as ol" +
            " on i.id = ol.item.id" +
            " join OrderEntity as o" +
            " on ol.order.id = o.id" +
            " where o.updatedOn between :fromDate and :toDate" +
            " and concat(lower(s.name), lower(s.email), lower(s.addressEntity.region),lower(s.addressEntity.city)," +
            " lower(s.addressEntity.street), lower(s.addressEntity.phone)) like lower(concat('%', :keyword,'%'))" +
            " and o.archives = true" +
            " group by s.id" +
            " order by s.name, turnover desc")
    List<Object[]> findSupplierTurnover(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("keyword") String keyword, Pageable pageable);

    @Query("select s.name from SupplierEntity AS s")
    List<String> findAllSupplierNames();
}
