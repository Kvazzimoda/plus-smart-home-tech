package ru.yandex.practicum.telemetry.analyzer.dai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "scenarios")
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hubId;

    private String name;

    @OneToMany
    @MapKeyColumn(
            table = "scenario_conditions",
            name = "sensor_id")
    @JoinTable(
            name = "scenario_conditions",
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns = @JoinColumn(name = "condition_id"))
    private Map<String, Condition> conditions = new HashMap<>();

    @OneToMany
    @MapKeyColumn(
            table = "scenario_actions",
            name = "sensor_id")
    @JoinTable(
            name = "scenario_actions",
            joinColumns = @JoinColumn(name = "scenario_id"),
            inverseJoinColumns = @JoinColumn(name = "action_id"))
    private Map<String, Action> actions = new HashMap<>();

    @Transient
    public void addCondition(String sensorId, Condition condition) {
        this.conditions.put(sensorId, condition);
    }

    @Transient
    public void addAction(String sensorId, Action action) {
        this.actions.put(sensorId, action);
    }
}
