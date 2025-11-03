package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    // Хранит снимки состояния
    private final Map<String, SensorsSnapshotAvro> sensors = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        // Находим снэпшот состояния датчиков конкретного хаба.
        // Если снэпшот ещё не вычислялся - создаём новый.
        final SensorsSnapshotAvro snapshot = sensors.computeIfAbsent(
                event.getHubId(),
                hubId ->
                        SensorsSnapshotAvro.newBuilder()
                                .setHubId(hubId)
                                .setTimestamp(event.getTimestamp())
                                .setSensorsState(new HashMap<>())
                                .build()
        );

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        if (sensorsState.containsKey(event.getId())) {
            SensorStateAvro oldState = sensorsState.get(event.getId());
            // если таймстемп у нового события раньше, чем таймстемп текущего состояния,
            // или данные нового события ничего не меняют по сравнению с текущими данными,
            // то игнорируем новое событие и ничего не обновляем
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                    oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        // Если дошли сюда, значит пришли новые данные и
        // снапшот нужно обновить
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
        sensorsState.put(event.getId(), newState);

        snapshot.setTimestamp(newState.getTimestamp());
        return Optional.of(snapshot);
    }
}