package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.category.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "select * from categories " +
            "order By id " +
            "limit :size " +
            "offset :from",
            nativeQuery = true)
    List<Category> findCategoriesWithFromAndSize(Long from, Long size);
}
