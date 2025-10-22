package ru.practicum.telemetry.collector.service.handler.hub;


import org.springframework.stereotype.Component;
import ru.practicum.telemetry.collector.mapper.ProtoToAvroMapper;
import ru.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import static ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto.ValueCase.BOOL_VALUE;

@Component
public class ScenarioAddedHubEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedHubEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED_PROTO;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto payload = event.getScenarioAddedProto();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(payload.getName())
                .setActions(
                        payload.getActionList()
                                .stream()
                                .map(this::mapToAvro)
                                .toList()
                )
                .setConditions(
                        payload.getConditionList()
                                .stream()
                                .map(this::mapToAvro)
                                .toList()
                )
                .build();
    }

    private DeviceActionAvro mapToAvro(DeviceActionProto action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ProtoToAvroMapper.map(action.getType(), ActionTypeAvro.class))
                .setValue(action.getValue())
                .build();
    }

    private ScenarioConditionAvro mapToAvro(ScenarioConditionProto condition) {
        return ScenarioConditionAvro.newBuilder()
                .setOperation(ProtoToAvroMapper.map(condition.getOperation(), ConditionOperationAvro.class))
                .setSensorId(condition.getSensorId())
                .setType(ProtoToAvroMapper.map(condition.getType(), ConditionTypeAvro.class))
                .setValue(condition.getValueCase().equals(BOOL_VALUE) ?
                        condition.getBoolValue() : condition.getIntValue())
                .build();
    }
}
