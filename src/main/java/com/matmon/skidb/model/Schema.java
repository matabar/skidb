package com.matmon.skidb.model;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;

public final class Schema {

    @Getter
    private final Map<String, FieldType> fieldTypes;

    public Schema(Map<String, FieldType> fieldTypes) {
        this.fieldTypes = Map.copyOf(Objects.requireNonNull(fieldTypes, "fieldTypes"));
    }
}
