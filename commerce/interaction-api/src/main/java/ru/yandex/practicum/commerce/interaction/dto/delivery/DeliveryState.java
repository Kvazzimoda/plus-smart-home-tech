package ru.yandex.practicum.commerce.interaction.dto.delivery;

public enum DeliveryState {
    CREATED,        // Создана
    IN_PROGRESS,    // В процессе
    DELIVERED,      // Доставлена
    FAILED,         // Не удалась
    CANCELLED       // Отменена
}