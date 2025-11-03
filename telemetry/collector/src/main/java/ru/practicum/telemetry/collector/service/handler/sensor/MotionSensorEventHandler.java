package ru.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Service;
import ru.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Service
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {
    public MotionSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_PROTO;
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEventProto event) {
        MotionSensorProto payload = event.getMotionSensorProto();
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(payload.getLinkQuality())
                .setVoltage(payload.getVoltage())
                .setMotion(payload.getMotion())
                .build();
    }
}
