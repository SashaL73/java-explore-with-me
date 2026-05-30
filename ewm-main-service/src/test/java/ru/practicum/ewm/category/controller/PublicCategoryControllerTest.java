package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCategoryController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PublicCategoryControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private final CategoryDto category = CategoryDto.builder()
            .name("Test")
            .id(1L)
            .build();

    @Test
    void getCategoriesTest() throws Exception {
        List<CategoryDto> categoryDtos = List.of(category);
        when(categoryService.getCategories(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(categoryDtos);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test"));

    }

    @Test
    void getCategoryTest() throws Exception {
        when(categoryService.getCategory(Mockito.anyLong()))
                .thenReturn(category);

        mockMvc.perform(get("/categories/{catId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void getCategoryWithIncorrectPathShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/categories/{catId}", 0L))
                .andExpect(status().isBadRequest());
    }

}
