package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceIntegrationTest {

    private final UserRepository userRepository;
    private final UserService userService;

    private final NewUserRequest newUserRequest = NewUserRequest.builder()
            .name("Test")
            .email("test@test.com")
            .build();
    private final User user = User.builder()
            .name("Test")
            .email("test@test.com")
            .build();


    @Test
    void createUserTest() {
        UserDto userDto = userService.createUser(newUserRequest);

        assertEquals("Test", userDto.name());
        assertEquals("test@test.com", userDto.email());
        assertNotNull(userDto.id());
    }

    @Test
    void createUserShouldReturnConflictException() {
        userRepository.save(user);
        assertThrows(ConflictException.class, () -> userService.createUser(newUserRequest));
    }

    @Test
    void getUsersTest() {
        User user1 = userRepository.save(user);
        List<Long> ids = List.of(user1.getId());
        List<UserDto> userDtoList = userService.getUsers(ids, 0L, 10L);
        List<UserDto> userDtoList1 = userService.getUsers(List.of(), 0L, 10L);
        List<UserDto> userDtoList2 = userService.getUsers(null, 0L, 10L);

        assertEquals(1, userDtoList.size());
        assertEquals("Test", userDtoList.getFirst().name());

        assertEquals(1, userDtoList1.size());
        assertEquals("Test", userDtoList1.getFirst().name());

        assertEquals(1, userDtoList2.size());
        assertEquals("Test", userDtoList2.getFirst().name());
    }

    @Test
    void deleteUserTest() {
        User user1 = userRepository.save(user);
        Long id = user1.getId();
        userService.deleteUser(id);
        Optional<User> userOptional = userRepository.findById(id);
        assertEquals(Optional.empty(), userOptional);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(id));
    }

}
