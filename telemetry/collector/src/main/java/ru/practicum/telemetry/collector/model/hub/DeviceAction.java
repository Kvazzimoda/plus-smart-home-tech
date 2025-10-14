package ru.practicum.telemetry.collector.model.hub;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceAction {
    private String sensorId;
    private DeviceActionType type;
    private Integer value; // допускает null
}