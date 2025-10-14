package ru.practicum.telemetry.collector.model.hub;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Перечисление типов событий хаба.")
public enum HubEventType {

    @Schema(description = "Событие добавления устройства.")
    DEVICE_ADDED_EVENT,
    @Schema(description = "Событие удаления устройства.")
    DEVICE_REMOVED_EVENT,
    @Schema(description = "Событие добавления сценария.")
    SCENARIO_ADDED_EVENT,
    @Schema(description = "Событие удаления сценария.")
    SCENARIO_REMOVED_EVENT
}
