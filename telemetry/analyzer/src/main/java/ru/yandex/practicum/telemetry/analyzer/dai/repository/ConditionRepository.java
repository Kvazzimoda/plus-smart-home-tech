package ru.yandex.practicum.telemetry.analyzer.dai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
