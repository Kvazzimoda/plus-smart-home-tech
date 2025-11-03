package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Action;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Condition;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Scenario;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.ScenarioRepository;

import java.time.Instant;
import java.util.Map;

import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SnapshotAnalyser {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotAnalyser(ScenarioRepository scenarioRepository,
                            @GrpcClient("hub-router")
                            HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterClient = hubRouterClient;
    }


    public void process(SensorsSnapshotAvro snapshot) {
        scenarioRepository
                .findByHubId(snapshot.getHubId())
                .stream()
                .filter(scenario -> isConditionsMatchSnapshot(snapshot, scenario.getConditions()))
                .forEach(this::performActions);
    }

    private boolean isConditionsMatchSnapshot(SensorsSnapshotAvro snapshot, Map<String, Condition> conditions) {
        return conditions
                .entrySet()
                .stream()
                .allMatch(conditionEntry ->
                        checkCondition(conditionEntry.getKey(), conditionEntry.getValue(), snapshot)
                );
    }

    private boolean checkCondition(String sensorId, Condition condition, SensorsSnapshotAvro snapshot) {
        if (!snapshot.getSensorsState().containsKey(sensorId)) {
            return false;
        }

        final SensorStateAvro state = snapshot.getSensorsState().get(sensorId);

        if(state.getData() instanceof ClimateSensorAvro data) {
            if(condition.getType().equals(TEMPERATURE)) {
                return condition.check(data.getTemperatureC());
            }
            if(condition.getType().equals(CO2LEVEL)) {
                return condition.check(data.getCo2Level());
            }
            if(condition.getType().equals((HUMIDITY))) {
                return condition.check(data.getHumidity());
            }
        }

        if(state.getData() instanceof LightSensorAvro data) {
            if(condition.getType().equals(LUMINOSITY)) {
                return condition.check(data.getLuminosity());
            }
        }

        if(state.getData() instanceof MotionSensorAvro data) {
            if(condition.getType().equals(MOTION)) {
                return condition.check(data.getMotion() ? 1 : 0);
            }
        }

        if(state.getData() instanceof TemperatureSensorAvro data) {
            if(condition.getType().equals(TEMPERATURE)) {
                return condition.check(data.getTemperatureC());
            }
        }

        if(state.getData() instanceof SwitchSensorAvro data) {
            if(condition.getType().equals(SWITCH)) {
                return condition.check(data.getState() ? 1 : 0);
            }
        }

        return false;
    }

    private void performActions(Scenario scenario) {
        log.debug("Сработал сценарий [{}] для хаба [{}]. Выполняю действия.",
                scenario.getName(), scenario.getHubId());
        Instant ts = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(ts.getEpochSecond())
                .setNanos(ts.getNano())
                .build();
        for (Map.Entry<String, Action> actionEntry : scenario.getActions().entrySet()) {
            Action scenarioAction = actionEntry.getValue();
            DeviceActionProto.Builder acctionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(actionEntry.getKey())
                    .setType(map(scenarioAction.getType()));
            if (scenarioAction.getType().equals(ActionTypeAvro.SET_VALUE)) {
                acctionBuilder.setValue(scenarioAction.getValue());
            }

            try {
                hubRouterClient.handleDeviceAction(
                        DeviceActionRequest.newBuilder()
                                .setHubId(scenario.getHubId())
                                .setScenarioName(scenario.getName())
                                .setAction(acctionBuilder.build())
                                .setTimestamp(timestamp)
                                .build()
                );
            } catch (Exception e) {
                log.error("Ошибка при отправке действия [{}] для хаба [{}] для устройства [{}]",
                        scenarioAction.getType().name(), scenario.getHubId(), scenarioAction.getId(), e);
            }
        }
    }

    private ActionTypeProto map(ActionTypeAvro avro) {
        for (ActionTypeProto value : ActionTypeProto.values()) {
            if(value.name().equalsIgnoreCase(avro.name())) {
                return value;
            }
        }
        return null;
    }
}
