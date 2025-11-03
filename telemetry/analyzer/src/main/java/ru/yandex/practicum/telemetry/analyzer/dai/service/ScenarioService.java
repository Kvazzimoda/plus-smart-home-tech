package ru.yandex.practicum.telemetry.analyzer.dai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Action;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Condition;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Scenario;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.SensorRepository;
import ru.yandex.practicum.telemetry.analyzer.exeptions.IllegalStateException;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ScenarioService {
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    public Scenario save(ScenarioAddedEventAvro event, String hubId) {
        //Собираем все используемые устройства
        Set<String> sensors = new HashSet<>();
        event.getConditions().forEach(condition -> sensors.add(condition.getSensorId()));
        event.getActions().forEach(action -> sensors.add(action.getSensorId()));

        //Проверяем принадлежность текущему хабу
        boolean allSensorsExists = sensorRepository.existsByIdInAndHubId(sensors, hubId);
        if(!allSensorsExists) {
            throw new IllegalStateException("Нет возможности создать сценарий с использованием неизвестного устройства");
        }

        //Проверяем есть ли уже сценарий с таким именем у хаба
        //Если нет, создаём новый объект scenario
        Optional<Scenario> maybeExist = scenarioRepository.findByHubIdAndName(hubId, event.getName());

        Scenario scenario;
        if(maybeExist.isEmpty()) {
            scenario = new Scenario();
            scenario.setName(event.getName());
            scenario.setHubId(hubId);
        } else {
            scenario = maybeExist.get();
            Map<String, Condition> conditions = scenario.getConditions();
            conditionRepository.deleteAll(conditions.values());
            scenario.getConditions().clear();

            Map<String, Action> actions = scenario.getActions();
            actionRepository.deleteAll(actions.values());
            scenario.getActions().clear();
        }

        for (ScenarioConditionAvro eventCondition : event.getConditions()) {
            Condition condition = new Condition();
            condition.setType(eventCondition.getType());
            condition.setOperation(ConditionOperation.from(eventCondition.getOperation()));
            condition.setValue(mapValue(eventCondition.getValue()));

            scenario.addCondition(eventCondition.getSensorId(), condition);
        }

        for (DeviceActionAvro eventAction : event.getActions()) {
            Action action = new Action();
            action.setType(eventAction.getType());
            if(eventAction.getType().equals(ActionTypeAvro.SET_VALUE)) {
                action.setValue(mapValue(eventAction.getValue()));
            }

            scenario.addAction(eventAction.getSensorId(), action);
        }

        //Сохраняем всё в базу
        conditionRepository.saveAll(scenario.getConditions().values());
        actionRepository.saveAll(scenario.getActions().values());
        return scenarioRepository.save(scenario);
    }

    //Удаляет сценарий по имени и ID хаба
    public void delete(String name, String hubId) {
        Optional<Scenario> optScenario = scenarioRepository.findByHubIdAndName(hubId, name);
        if(optScenario.isPresent()) {
            Scenario scenario = optScenario.get();
            conditionRepository.deleteAll(scenario.getConditions().values());
            actionRepository.deleteAll(scenario.getActions().values());
            scenarioRepository.delete(scenario);
        }
    }

    private Integer mapValue(Object value) {
        if (value != null) {
            if (value instanceof Integer i) return i;
            if (value instanceof Boolean b) return b ? 1 : 0;
        }
        return null;
    }

}
