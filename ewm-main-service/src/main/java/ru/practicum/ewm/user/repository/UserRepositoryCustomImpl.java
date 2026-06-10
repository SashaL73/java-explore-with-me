package ru.practicum.ewm.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.user.model.QUser;
import ru.practicum.ewm.user.model.User;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<User> searchUsers(List<Long> ids, Long from, Long size) {
        QUser user = QUser.user;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (ids != null && !ids.isEmpty()) {
            booleanBuilder.and(user.id.in(ids));
        }

        return jpaQueryFactory.selectFrom(user)
                .where(booleanBuilder)
                .orderBy(user.id.asc())
                .offset(from)
                .limit(size)
                .fetch();
    }
}
