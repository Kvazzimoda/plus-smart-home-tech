package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.dai.entity.Sensor;
import ru.yandex.practicum.telemetry.analyzer.dai.service.ScenarioService;
import ru.yandex.practicum.telemetry.analyzer.dai.service.SensorService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final SensorService sensorService;
    private final ScenarioService scenarioService;
    private final List<String> topics;
    private final Duration pollTimeout;

    public HubEventProcessor(KafkaConfig config, SensorService sensorService, ScenarioService scenarioService) {
        this.sensorService = sensorService;
        this.scenarioService = scenarioService;

        final KafkaConfig.ConsumerConfig consumerConfig =
                config.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера событий от хабов. ");
            consumer.wakeup();
        }));
    }

    @Override
    public void run() {
        log.trace("Подписываюсь на топики {}", topics);
        consumer.subscribe(topics);
        try {
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        processEvent(record.value());
                    }
                    // Добавление/удаление устройств и сценариев - редкие события.
                    // Поэтому поток сообщений будет не интенсивный.
                    // Так что имеет смысл фиксировать смещения синхронно.
                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            // завершаем работу консьюмера (в блоке final)
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хабов", e);
        } finally {
            consumer.close();
        }
    }

    private void processEvent(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        switch (hubEvent.getPayload()) {
            case DeviceAddedEventAvro dae -> processEvent(hubId, dae);
            case DeviceRemovedEventAvro dre -> processEvent(hubId, dre);
            case ScenarioAddedEventAvro sae -> processEvent(hubId, sae);
            case ScenarioRemovedEventAvro sre -> processEvent(hubId, sre);
            default -> log.warn("Получено событие неизвестного типа {}", hubEvent);
        }
    }

    private void processEvent(String hubId, DeviceAddedEventAvro event) {
        Optional<Sensor> maybeAdded = sensorService.findByIdAndHubId(hubId, event.getId());
        if (maybeAdded.isPresent()) {
            log.info("Устройство с id [{}] уже зарегистрировано в хабе [{}]", event.getId(), hubId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setHubId(hubId);
        sensor.setId(event.getId());

        log.debug("В хабе [{}] зарегистрирован новый датчик: [{}]", hubId, event.getId());
        sensorService.save(sensor);
    }

    private void processEvent(String hubId, DeviceRemovedEventAvro event) {
        log.debug("Удаляю датчик [{}] из хаба [{}]", event.getId(), hubId);
        sensorService
                .findByIdAndHubId(event.getId(), hubId)
                .ifPresent(sensorService::delete);
    }

    private void processEvent(String hubId, ScenarioAddedEventAvro event) {
        log.info("Получил запрос на добавление нового сценария {} для хаба {}",
                event.getName(), hubId);

        scenarioService.save(event, hubId);
    }

    private void processEvent(String hubId, ScenarioRemovedEventAvro event) {
        log.info("Получил запрос на удаление сценария {} из хаба {}", event.getName(), hubId);
        scenarioService.delete(event.getName(), hubId);
    }
}
