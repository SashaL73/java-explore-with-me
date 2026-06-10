package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserStatus;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class CategoryServiceIntegrationTest {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final NewCategoryDto newCategoryDto = NewCategoryDto.builder()
            .name("Test")
            .build();

    private final Category category = Category.builder()
            .name("Test")
            .build();

    private final User user = User.builder()
            .name("Test")
            .email("test@test.com")
            .status(UserStatus.PUBLIC)
            .build();

    private final CategoryDto categoryDto = CategoryDto.builder()
            .name("UpdatedTest")
            .build();

    private final Event event = Event.builder()
            .createdOn(LocalDateTime.now())
            .annotation("TestAnnotation")
            .category(category)
            .description("TestDescription")
            .eventDate(LocalDateTime.now().plusHours(2))
            .initiator(user)
            .location(Location.builder()
                    .lat(10.1F)
                    .lon(10.1F)
                    .build())
            .paid(true)
            .participantLimit(0L)
            .requestModeration(true)
            .eventState(EventState.PENDING)
            .title("TestTitle")
            .build();


    @Test
    void createCategoryTest() {
        CategoryDto categoryDto = categoryService.createCategory(newCategoryDto);

        assertEquals(newCategoryDto.name(), categoryDto.name());
        assertNotNull(categoryDto.id());
    }

    @Test
    void createCategoryWithExistNameShouldReturnConflictException() {
        categoryRepository.save(category);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(newCategoryDto));
    }

    @Test
    void deleteCategoryTest() {
        Category savedCategory = categoryRepository.save(category);

        categoryService.deleteCategory(savedCategory.getId());

        Optional<Category> optionalCategory = categoryRepository.findById(savedCategory.getId());

        assertEquals(Optional.empty(), optionalCategory);
    }

    @Test
    void deleteCategoryWithUsedCategoryShouldReturnConflictException() {
        userRepository.save(user);
        Category category1 = categoryRepository.save(category);
        eventRepository.save(event);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory(category1.getId()));

    }

    @Test
    void deleteCategoryNotExistShouldReturnNotFoundException() {
        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(999L));
    }

    @Test
    void updateCategoryTest() {
        Category category1 = categoryRepository.save(category);


        CategoryDto updatedCategory = categoryService.updateCategory(category1.getId(), categoryDto);

        assertEquals(categoryDto.name(), updatedCategory.name());
    }

    @Test
    void updateCategoryExistNameCategoryShouldReturnConflict() {
        Category category1 = categoryRepository.save(category);
        Category category2 = Category.builder()
                .name("Test1")
                .build();
        categoryRepository.save(category2);

        CategoryDto categoryDto1 = CategoryDto.builder()
                .name("Test1")
                .build();

        assertThrows(ConflictException.class, () -> categoryService.updateCategory(category1.getId(), categoryDto1));
    }

    @Test
    void updateCategoryNotExistCategoryShouldReturnNotFoundException() {
        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(999L, categoryDto));
    }

    @Test
    void getCategoriesTest() {
        categoryRepository.save(category);
        Category category2 = Category.builder()
                .name("Test1")
                .build();
        categoryRepository.save(category2);

        List<CategoryDto> categoryDtoList = categoryService.getCategories(0L, 10L);

        assertEquals(2, categoryDtoList.size());
        categoryDtoList.forEach(c -> assertNotNull(c.id()));
    }

    @Test
    void getCategoryTest() {
        Category savedCategory = categoryRepository.save(category);
        CategoryDto findCategory = categoryService.getCategory(savedCategory.getId());

        assertEquals("Test", findCategory.name());
        assertEquals(savedCategory.getId(), findCategory.id());
    }

    @Test
    void getCategoryNotExistShouldReturnNotFoundException() {
        assertThrows(NotFoundException.class, () -> categoryService.getCategory(999L));
    }


}
