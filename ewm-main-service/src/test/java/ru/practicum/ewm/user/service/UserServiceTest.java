package ru.practicum.ewm.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    void createUserTest() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .email("test@test.com")
                .name("Test")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@test.com")
                .build();

        when(userRepository.saveAndFlush(Mockito.any(User.class))).thenReturn(user);
        UserDto userDto = userServiceImpl.createUser(newUserRequest);
        assertEquals(userDto.name(), newUserRequest.name());
        assertEquals(userDto.email(), newUserRequest.email());
        Mockito.verify(userRepository).saveAndFlush(Mockito.any(User.class));
    }

    @Test
    void getUsersTest() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@test.com")
                .build();

        when(userRepository
                .searchUsers(Mockito.anyList(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(List.of(user));
        List<UserDto> userDtoList = userServiceImpl.getUsers(List.of(1L), 0L, 10L);

        assertEquals(1, userDtoList.size());
        assertEquals("Test", userDtoList.getFirst().name());
        assertEquals("test@test.com", userDtoList.getFirst().email());
        Mockito.verify(userRepository).searchUsers(Mockito.anyList(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void deleteUserTest() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@test.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userServiceImpl.deleteUser(1L);

        Mockito.verify(userRepository).findById(1L);
        Mockito.verify(userRepository).deleteById(1L);
    }

}
