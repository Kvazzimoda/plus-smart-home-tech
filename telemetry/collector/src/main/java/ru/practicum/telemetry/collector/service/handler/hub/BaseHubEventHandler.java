package ru.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.practicum.telemetry.collector.model.hub.HubEvent;
import ru.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.practicum.telemetry.collector.service.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
public abstract class BaseHubEventHandler<T extends SpecificRecordBase> implements HubEventHandler {

    protected final KafkaEventProducer kafkaEventProducer;
    protected final String topic;

    public BaseHubEventHandler(KafkaEventProducer kafkaEventProducer, String topic) {
        this.kafkaEventProducer = kafkaEventProducer;
        this.topic = topic;
    }

    @Override
    public void handle(HubEvent hubEvent) {
        try {
            Producer<String, SpecificRecordBase> producer = kafkaEventProducer.getProducer();
            T specificAvroEvent = mapToAvro(hubEvent);
            HubEventAvro avroEvent = HubEventAvro.newBuilder()
                    .setHubId(hubEvent.getHubId())
                    .setTimestamp(hubEvent.getTimestamp())
                    .setPayload(specificAvroEvent)
                    .build();
            log.info("Начинаю отправку сообщений {} в топик {}", avroEvent, topic);

            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, avroEvent);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки сообщения в топик {}", topic, exception);
                } else {
                    log.info("Сообщение отправлено в топик {} partition {} offset {}",
                            topic, metadata.partition(), metadata.offset());
                }
            });
            producer.flush();
        } catch (Exception e) {
            log.error("Ошибка обработки события", e);
        }
    }

    protected abstract T mapToAvro(HubEvent hubEvent);
}