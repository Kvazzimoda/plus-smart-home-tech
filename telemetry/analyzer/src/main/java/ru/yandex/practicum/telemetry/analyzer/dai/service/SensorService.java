package ru.yandex.practicum.telemetry.analyzer.dai.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Sensor;
import ru.yandex.practicum.telemetry.analyzer.dai.repository.SensorRepository;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SensorService {
    private final SensorRepository repository;

    public Sensor save(Sensor sensor) {
        return repository.save(sensor);
    }

    @Transactional(readOnly = true)
    public Optional<Sensor> findByIdAndHubId(String id, String hubId) {
        return repository.findByIdAndHubId(id, hubId);
    }

    public void delete(Sensor sensor) {
        repository.delete(sensor);
    }
}
