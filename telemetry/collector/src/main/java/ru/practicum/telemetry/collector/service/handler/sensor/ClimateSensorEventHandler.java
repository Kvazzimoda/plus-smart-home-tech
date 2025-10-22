package ru.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {

    public ClimateSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_PROTO;
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEventProto event) {
        ClimateSensorProto payload = event.getClimateSensorProto();
        return ClimateSensorAvro.newBuilder()
                .setCo2Level(payload.getCo2Level())
                .setHumidity(payload.getHumidity())
                .setTemperatureC(payload.getTemperatureC())
                .build();
    }
}
