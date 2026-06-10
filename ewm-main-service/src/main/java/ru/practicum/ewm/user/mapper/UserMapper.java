package ru.practicum.ewm.user.mapper;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.model.UserStatus;

public class UserMapper {
    public static User mapToUser(NewUserRequest request) {
        return User.builder()
                .email(request.email())
                .name(request.name())
                .status(UserStatus.PUBLIC)
                .build();
    }

    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .id(user.getId())
                .status(user.getStatus())
                .build();
    }

    public static UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();

    }
}
