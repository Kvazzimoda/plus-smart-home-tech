package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final SnapshotAnalyser analyser;

    // Хранит текущий обработанный оффсет для каждой партиции и топика
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    // Консьюмер для получения снэпшотов
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;

    @Autowired
    public SnapshotProcessor(SnapshotAnalyser analyser, KafkaConfig kafkaConfig) {
        this.analyser = analyser;

        final KafkaConfig.ConsumerConfig consumerConfig =
                kafkaConfig.getConsumers().get(this.getClass().getSimpleName());

        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера снапшотов. ");
            consumer.wakeup();
        }));
    }


    public void start() {
        try {
            log.trace("Подписываюсь на топик \"{}\" для получения снапшотов", topics);
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);

                if(!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        // Обрабатываем очередную запись
                        analyser.process(record.value());

                        // Фиксируем оффсеты обработанных записей, если нужно
                        manageOffsets(record, count++);
                    }
                    // Фиксируем максимальный оффсет обработанных записей
                    consumer.commitAsync();
                }
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            // Закрываем консьюмер, но перед этим убеждаемся,
            // что все оффсеты обработанных сообщений зафиксированы
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<?, ?> record, int count) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if(count % 100 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}
