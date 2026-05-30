package ru.practicum.ewm.compilation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.QCompilation;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompilationRepositoryCustomImpl implements CompilationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Compilation> searchCompilations(Boolean pinned, Long from, Long size) {
        QCompilation compilation = QCompilation.compilation;

        BooleanBuilder builder = new BooleanBuilder();

        if (pinned != null) {
            builder.and(compilation.pinned.eq(pinned));
        }

        return jpaQueryFactory
                .selectFrom(compilation)
                .leftJoin(compilation.events).fetchJoin()
                .where(builder)
                .orderBy(compilation.id.asc())
                .offset(from)
                .limit(size)
                .fetch();
    }
}
