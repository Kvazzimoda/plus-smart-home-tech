package ru.yandex.practicum.telemetry.analyzer.exeptions;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class IllegalStateException extends StatusRuntimeException {
    public IllegalStateException(String message) {
        super(Status.FAILED_PRECONDITION.withDescription(message));
    }
}
