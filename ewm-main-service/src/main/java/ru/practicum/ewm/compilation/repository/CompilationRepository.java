package ru.practicum.ewm.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, CompilationRepositoryCustom {

    @Query("select distinct c " +
            "from Compilation c " +
            "left join fetch c.events e " +
            "left join fetch e.category " +
            "left join fetch e.initiator " +
            "where c.id = :compId"
    )
    Optional<Compilation> findByIdWithEvents(@Param("compId") Long compId);

}
