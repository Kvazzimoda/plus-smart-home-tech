package ru.practicum.telemetry.collector.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtoToAvroMapper {

    public static <E1 extends Enum<E1>, E2 extends Enum<E2>> E2 map(E1 source, Class<E2> targetClass) {
        for (E2 target : EnumSet.allOf(targetClass)) {
            if (target.name().equalsIgnoreCase(source.name())) {
                return target;
            }
        }
        return null;
    }
}