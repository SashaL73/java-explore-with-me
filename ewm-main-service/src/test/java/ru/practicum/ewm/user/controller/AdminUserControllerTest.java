package ru.practicum.ewm.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminUserControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private final NewUserRequest newUserRequest = NewUserRequest.builder()
            .name("Test")
            .email("test@test.com")
            .build();

    @Test
    void createUserTest() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Mockito.verify(userService).createUser(Mockito.any(NewUserRequest.class));
    }

    @Test
    void createUserIncorrectEmailShouldReturnBadRequest() throws Exception {
        NewUserRequest badRequest = NewUserRequest.builder()
                .name("Test")
                .email("test.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserIncorrectNameShouldReturnBadRequest() throws Exception {
        NewUserRequest badRequest = NewUserRequest.builder()
                .email("test@test.com")
                .build();
        mockMvc.perform(post("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsersTest() throws Exception {
        List<UserDto> userDtoList = List.of(UserDto.builder()
                .name("Test")
                .email("test@test.com")
                .id(1L)
                .build());

        when(userService.getUsers(
                Mockito.anyList(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(userDtoList);

        mockMvc.perform(get("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("ids", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].email").value("test@test.com"));

    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserWhitIncorrectIdPathShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 0L))
                .andExpect(status().isBadRequest());
    }


}
