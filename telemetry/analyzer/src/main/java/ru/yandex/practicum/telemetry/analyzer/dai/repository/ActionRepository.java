package ru.yandex.practicum.telemetry.analyzer.dai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
