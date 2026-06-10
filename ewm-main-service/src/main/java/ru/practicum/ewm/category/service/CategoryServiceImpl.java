package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.event.repository.EventRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.mapToCategory(newCategoryDto);
        category = saveCategoryOrThrow(category);
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        findCategoryOrThrow(catId);

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория id=" + catId + " используется");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = findCategoryOrThrow(catId);
        if (!Objects.isNull(categoryDto.name()) && !categoryDto.name().isBlank()) {
            category.setName(categoryDto.name());
            category = saveCategoryOrThrow(category);
        }

        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public List<CategoryDto> getCategories(Long from, Long size) {
        Pageable pageable = PageRequest.of(Math.toIntExact(from), Math.toIntExact(size));

        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = findCategoryOrThrow(catId);
        return CategoryMapper.mapToCategoryDto(category);
    }

    private Category findCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
    }

    private Category saveCategoryOrThrow(Category category) {
        try {
            return categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Категория с таким названием уже существует");
        }
    }
}
