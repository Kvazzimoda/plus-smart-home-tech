package ru.practicum.telemetry.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.collector.configuration.KafkaProducerProperties;

import static ru.practicum.telemetry.collector.configuration.KafkaProducerProperties.TopicType;


import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
public class KafkaEventProducer implements AutoCloseable {

    protected final KafkaProducer<String, SpecificRecordBase> producer;
    protected final EnumMap<TopicType, String> topics;

    public KafkaEventProducer(KafkaProducerProperties kafkaProducerProperties) {
        this.topics = kafkaProducerProperties.getProducer().getTopics();

        // Создаём продюсера используя настройки из конфигурации приложения
        this.producer = new KafkaProducer<>(kafkaProducerProperties.getProducer().getProperties());
    }


    public void send(SpecificRecordBase event, String hubId, Instant timestamp, TopicType topicType) {
        String topic = topics.get(topicType);
        // Формируем запись для отправки в топик, при этом указываем ключ записи - это id хаба
        // это означает, что запись будет сохраняться в партицию в зависимости от id хаба, а это
        // в свою очередь означает, что записи относящиеся к одному хабу можно будет читать упорядоченно
        // т.к. кафка гарантирует очередность сообщений только в рамках партиции.
        // Также мы указываем таймстемп записи и используем для этого время возникновения события
        // это значит, что кафка будет упорядочивать записи по времени возникновения события, а не времени
        // когда брокер кафки получил сообщение
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,                    // Имя топика куда будет осуществлена запись
                null,                     // Номер партиции (если null, то используется ключ для вычисления раздела)
                timestamp.toEpochMilli(), // Метка времени события
                hubId,                    // Ключ события
                event                     // Значение события
        );

        // Логирование сохранения события
        String eventClass = event.getClass().getSimpleName();
        log.trace("Сохраняю событие {} связанное с хабом {} в топик {}",
                eventClass, hubId, topic);

        // Отправка события в топик Kafka
        Future<RecordMetadata> futureResult = producer.send(record);
        producer.flush();
        try {
            RecordMetadata metadata = futureResult.get();
            log.info("Событие {} было успешно сохранёно в топик {} в партицию {} со смещением {}",
                    eventClass, metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Не удалось записать событие {} в топик {}", eventClass, topic, e);
        }
    }

    @Override
    public void close() {
        // Данный метод из AutoCloseable, Spring будет автоматически добавлять бины
        // которые реализуют AutoCloseable/Disposable в shutdown hook при закрытии jvm
        // и вызывать их метод close

        // отправляем оставшиеся данные и закрываем продюсер
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}