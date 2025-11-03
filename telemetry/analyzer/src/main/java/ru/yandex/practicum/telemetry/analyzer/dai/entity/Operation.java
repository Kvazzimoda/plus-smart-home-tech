package ru.yandex.practicum.telemetry.analyzer.dai.entity;

public interface Operation {
    boolean apply(Integer left, Integer right);
}
