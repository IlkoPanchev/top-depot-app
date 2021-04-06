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

    @Query("select c.companyName as company_name," +
            " c.personName as person_name," +
            " sum(ol.subtotal) as turnover," +
            " count(distinct o.id) as orders_count," +
            " sum(ol.quantity) as items_count from CustomerEntity as c" +
            " join AddressEntity as a" +
            " on c.addressEntity.id = a.id" +
            " join OrderEntity as o" +
            " on c.id = o.customer.id" +
            " join OrderLineEntity as ol" +
            " on o.id = ol.order.id" +
            " join ItemEntity as i" +
            " on ol.item.id = i.id" +
            " where o.updatedOn between :fromDate and :toDate" +
            " and concat(lower(c.companyName), lower(c.personName), lower(c.email)," +
            " lower(a.region),lower(a.city), lower(a.street), lower(a.phone)) LIKE lower(concat('%', :keyword,'%'))" +
            " and o.archives = true group by c.id order by c.companyName, c.personName, turnover desc")
    List<Object[]> findCustomerTurnover(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("keyword") String keyword, Pageable pageable);
}
