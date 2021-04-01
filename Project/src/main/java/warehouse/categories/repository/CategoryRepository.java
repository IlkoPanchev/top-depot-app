package warehouse.categories.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import warehouse.categories.model.CategoryEntity;
import warehouse.items.model.ItemEntity;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByName(String name);

    @Query("SELECT c FROM CategoryEntity c WHERE CONCAT(lower(c.name), lower(c.description)) LIKE lower(concat('%', ?1,'%'))")
    Page<CategoryEntity> search(String keyword, Pageable pageable);

    @Query("select c.name from CategoryEntity AS c")
    List<String> findAllCategoryNames();
}
