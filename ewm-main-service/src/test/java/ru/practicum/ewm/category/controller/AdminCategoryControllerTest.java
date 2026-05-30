package ru.practicum.ewm.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminCategoryControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void createCategoryTest() throws Exception {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name("Test")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void createCategoryWithIncorrectNameShouldReturnBadRequest() throws Exception {
        NewCategoryDto newCategoryDto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategoryTest() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategoryWithIncorrectPathIdTest() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategoryTest() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("Test")
                .build();

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateCategoryWithIncorrectNameShouldReturnBadRequest() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("t".repeat(51))
                .build();

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void updateCategoryWithCategoryWithIncorrectPathId() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("Test")
                .build();

        mockMvc.perform(patch("/admin/categories/{catId}", 0L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
