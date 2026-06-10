package ru.practicum.ewm.compilation.repository;

import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepositoryCustom {

    List<Compilation> searchCompilations(Boolean pinned, Long from, Long size);
}
