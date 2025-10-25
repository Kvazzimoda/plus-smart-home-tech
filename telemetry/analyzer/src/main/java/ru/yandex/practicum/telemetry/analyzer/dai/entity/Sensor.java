package ru.yandex.practicum.telemetry.analyzer.dai.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
@Getter
@Setter
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    private String hubId;
}