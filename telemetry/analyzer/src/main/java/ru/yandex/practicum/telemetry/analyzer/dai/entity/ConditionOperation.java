package ru.yandex.practicum.telemetry.analyzer.dai.entity;


import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;

public enum ConditionOperation implements Operation {
    EQUALS {
        @Override
        public boolean apply(Integer left, Integer right) {
            if(left != null && right != null) {
                return left.compareTo(right) == 0;
            }
            return false;
        }
    },
    GREATER_THAN {
        @Override
        public boolean apply(Integer left, Integer right) {
            if(left != null && right != null) {
                return left.compareTo(right) > 0;
            }
            return false;
        }
    },
    LOWER_THAN {
        @Override
        public boolean apply(Integer left, Integer right) {
            if(left != null && right != null) {
                return left.compareTo(right) < 0;
            }
            return false;
        }
    };

    public static ConditionOperation from(ConditionOperationAvro operation) {
        for (ConditionOperation value : values()) {
            if(value.name().equalsIgnoreCase(operation.name())) {
                return value;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип операции: " + operation.name());
    }
}
