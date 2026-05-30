package ru.practicum.ewm.category.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;

    private final NewCategoryDto newCategoryDto = NewCategoryDto
            .builder()
            .name("Test")
            .build();

    private final Category category = Category.builder()
            .name("Test")
            .id(1L)
            .build();

    private final CategoryDto categoryDto = CategoryDto.builder()
            .name("Test")
            .id(1L)
            .build();

    @Test
    void createCategoryTest() {
        Mockito.when(categoryRepository.saveAndFlush(Mockito.any(Category.class)))
                .thenReturn(category);

        CategoryDto categoryDto = categoryServiceImpl.createCategory(newCategoryDto);

        assertEquals(categoryDto.name(), newCategoryDto.name());
    }

    @Test
    void deleteCategoryTest() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(category));

        Mockito.when(eventRepository.existsByCategoryId(Mockito.anyLong()))
                .thenReturn(false);

        categoryServiceImpl.deleteCategory(1L);

        Mockito.verify(categoryRepository).findById(1L);
        Mockito.verify(eventRepository).existsByCategoryId(1L);
    }

    @Test
    void deleteUsedCategoryShouldReturnConflictExceptionTest() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(category));

        Mockito.when(eventRepository.existsByCategoryId(Mockito.anyLong()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryServiceImpl.deleteCategory(1L));
    }

    @Test
    void deleteNotExistCategoryShouldReturnNotFoundExceptionTest() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryServiceImpl.deleteCategory(1L));

    }

    @Test
    void updateCategoryTest() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(category));

        Mockito.when(categoryRepository.saveAndFlush(Mockito.any(Category.class)))
                .thenReturn(category);

        CategoryDto category = categoryServiceImpl.updateCategory(1L, categoryDto);

        assertEquals(categoryDto.name(), category.name());

    }

    @Test
    void updateCategoryWithEmptyOrNullNameShouldReturnCategory() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(category));

        CategoryDto requestNullName = CategoryDto.builder()
                .id(1L)
                .build();

        CategoryDto categoryDto1 = categoryServiceImpl.updateCategory(1L, requestNullName);
        assertEquals(categoryDto.name(), categoryDto1.name());

        CategoryDto requestBlackName = CategoryDto.builder()
                .name(" ")
                .id(1L)
                .build();

        CategoryDto categoryDto2 = categoryServiceImpl.updateCategory(1L, requestBlackName);
        assertEquals(categoryDto.name(), categoryDto2.name());
    }

    @Test
    void updateNotExistCategoryShouldReturnNotFoundException() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryServiceImpl.updateCategory(1L, categoryDto));
    }

    @Test
    void getCategoriesTest() {
        List<Category> categories = List.of(category);

        Mockito.when(categoryRepository.findCategoriesWithFromAndSize(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(categories);

        List<CategoryDto> categoryDtos = categoryServiceImpl.getCategories(0L, 10L);

        assertEquals(1, categoryDtos.size());
    }

    @Test
    void getCategoryTest() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));

        CategoryDto categoryDto1 = categoryServiceImpl.getCategory(1L);

        assertEquals(category.getName(), categoryDto1.name());

    }

    @Test
    void getNotExistCategoryShouldReturnNotFoundException() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryServiceImpl.getCategory(1L));
    }

}
