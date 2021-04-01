package warehouse.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import warehouse.items.model.ItemEntity;
import warehouse.users.model.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE CONCAT(lower(u.username)," +
            " lower(u.email), lower(u.department.departmentName)) LIKE lower(concat('%', ?1,'%'))")
    Page<UserEntity> search(String keyword, Pageable pageable);
}
