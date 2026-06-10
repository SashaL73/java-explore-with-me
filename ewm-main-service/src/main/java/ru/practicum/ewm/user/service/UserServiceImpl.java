package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание пользователя ={}", newUserRequest);
        User user = UserMapper.mapToUser(newUserRequest);

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(newUserRequest.email() + " уже существует");
        }

        log.info("Пользователь создан с id={}", user.getId());
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Long from, Long size) {
        log.info("Получение списка пользователей ids={}", ids);
        List<User> users = userRepository.searchUsers(ids, from, size);
        return users.stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя id={}", id);
        findUserOrThrow(id);
        userRepository.deleteById(id);
    }

    private void findUserOrThrow(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = " + id + " нет"));

    }
}
