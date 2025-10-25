package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.KafkaConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SnapshotService snapshotService;

    // хранит текущий обработанный оффсет
    private final Map<TopicPartition, OffsetAndMetadata> currentOffSets = new HashMap<>();

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaConfig.ConsumerConfig consumerConfig;

    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaConfig.ProducerConfig producerConfig;

    @Autowired
    public AggregationStarter(SnapshotService snapshotService, KafkaConfig kafkaConfig) {
        this.snapshotService = snapshotService;

        this.consumerConfig = kafkaConfig.getConsumer();
        this.producerConfig = kafkaConfig.getProducer();

        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.producer = new KafkaProducer<>(producerConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Завершаю работу консьюмера");
            consumer.wakeup();
        }));
    }

    public void start() {
        try {
            log.trace("Подписываюсь на топик \"{}\" для получения событий от датчиков", consumerConfig.getTopic());
            consumer.subscribe(List.of(consumerConfig.getTopic()));

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(consumerConfig.getPollTimeout());

                if(!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<String, SensorEventAvro> record : records) {
                        log.trace("Обрабатываю сообщение от хаба {} из партиции {} со смещением: {}",
                                record.key(), record.partition(), record.offset());
                        handleEvent(record.value());

                        manageOffsets(record, count++);
                    }
                    producer.flush();
                    consumer.commitAsync();
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffSets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void handleEvent(SensorEventAvro event) {
        // Если полученное событие от датчика содержит новые значения, то обновляем состояние датчиков хаба данными
        // от полученного события
        Optional<SensorsSnapshotAvro> updatedState = snapshotService.updateState(event);

        // Если состояние было обновлено, отправляем его в топик снэпшотов
        if(updatedState.isPresent()) {
            SensorsSnapshotAvro sensorsSnapshot = updatedState.get();

            log.info("Событие датчика {} обновило состояние снапшота. " +
                            "Сохраняю снапшот состояния датчиков хаба {} от {} в топик {}",
                    event.getId(), sensorsSnapshot.getHubId(), sensorsSnapshot.getTimestamp(),
                    producerConfig.getTopic());

            ProducerRecord<String, SensorsSnapshotAvro> recordToSend =
                    new ProducerRecord<>(
                            producerConfig.getTopic(),
                            null,
                            sensorsSnapshot.getTimestamp().toEpochMilli(),
                            sensorsSnapshot.getHubId(),
                            sensorsSnapshot
                    );

            Future<RecordMetadata> futureResult = producer.send(recordToSend);
            producer.flush(); // Это нужно только для тестового пайплайна, что
            try {
                RecordMetadata metadata = futureResult.get();
                log.info("Снапшот был сохранён в партицию {} со смещением {}", metadata.partition(), metadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Не удалось записать снапшот в топик {}", producerConfig.getTopic(), e);
            }
        } else {
            log.trace("Событие от датчика {} хаба {} не обновило состояние снапшота", event.getId(), event.getHubId());
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorEventAvro> record, int count) {
        currentOffSets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if(count % 200 == 0) {
            consumer.commitAsync(currentOffSets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}
