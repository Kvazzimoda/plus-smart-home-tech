package ru.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.practicum.telemetry.collector.mapper.ProtoToAvroMapper;
import ru.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;

@Component
public class DeviceAddedHubEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    public DeviceAddedHubEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED_PROTO;
    }

    @Override
    protected DeviceAddedEventAvro mapToAvro(HubEventProto eventProto) {
        DeviceAddedEventProto protoPayload = eventProto.getDeviceAddedProto();
        return DeviceAddedEventAvro.newBuilder()
                .setId(protoPayload.getId())
                .setType(ProtoToAvroMapper.map(protoPayload.getType(), DeviceTypeAvro.class))
                .build();
    }
}
