package ru.practicum.ewm.user.repository;

import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserRepositoryCustom {

    List<User> searchUsers(List<Long> ids, Long from, Long size);
}
