package warehouse.orders.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import warehouse.orders.model.OrderEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByDeletedFalse(Pageable pageable);

    List<OrderEntity> findAllByUpdatedOnBeforeAndClosedFalseAndArchivesFalseAndDeletedFalse(LocalDateTime upTo);

    @Query("SELECT o FROM OrderEntity o WHERE o.deleted = false AND CONCAT(concat(o.createdOn, ''), concat(o.updatedOn, ''), lower(o.customer.companyName), lower(o.customer.personName)," +
            " concat(o.total, '')) LIKE lower(concat('%', ?1,'%'))")
    Page<OrderEntity> search(String keyword, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.closed = false AND o.archives = false AND o.deleted = false AND o.createdOn < o.updatedOn ORDER BY o.updatedOn DESC")
    Page<OrderEntity> findAllOrderByUpdatedOnDesc(Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.closed = true AND o.archives = false AND o.deleted = false ORDER BY o.updatedOn DESC")
    Page<OrderEntity> findAllCompletedOrdersByUpdatedOnDesc(Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.closed = false AND o.archives = false  AND o.deleted = false ORDER BY o.createdOn DESC")
    Page<OrderEntity> findAllOrdersByCreatedOnDesc(Pageable pageable);

    List<OrderEntity> findAllByCreatedOnBetweenAndClosedFalseAndArchivesFalseAndDeletedFalse(LocalDateTime dateFrom, LocalDateTime dateTo);

    List<OrderEntity> findAllByUpdatedOnBetweenAndClosedTrueAndArchivesFalse(LocalDateTime dateFrom, LocalDateTime dateTo);

    List<OrderEntity> findAllByUpdatedOnBetweenAndClosedTrueAndArchivesTrue(LocalDateTime dateFrom, LocalDateTime dateTo);

    List<OrderEntity> findAllByUpdatedOnBetweenAndArchivesTrueOrderByUpdatedOnAsc(LocalDateTime weekStart, LocalDateTime weekEnd);

    List<OrderEntity> findAllByArchivesTrueOrderByUpdatedOnAsc();

    List<OrderEntity> findAllByOrderByCreatedOnAsc();

}
