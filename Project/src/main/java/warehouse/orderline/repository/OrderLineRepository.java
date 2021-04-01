package warehouse.orderline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import warehouse.orderline.model.OrderLineEntity;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLineEntity, Long> {
}
