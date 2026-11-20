package com.matmon.skidb.engine;

import com.matmon.skidb.exception.SchemaViolationException;
import com.matmon.skidb.exception.UnknownFieldException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SchemaValidator {

    static void validateDocument(Schema schema, Map<String, Object> document) {
        for (String field : schema.getFieldTypes().keySet()) {
            if (!document.containsKey(field)) {
                throw new SchemaViolationException("missing field: " + field);
            }
        }
        checkValues(schema, document);
    }

    static void validateQuery(Schema schema, Map<String, Object> query) {
        checkValues(schema, query);
    }

    private static void checkValues(Schema schema, Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            FieldType type = schema.getFieldTypes().get(entry.getKey());
            if (type == null) {
                throw new UnknownFieldException("unknown field: " + entry.getKey());
            }
            checkFieldType(type, entry.getKey(), entry.getValue());
        }
    }

    private static void checkFieldType(FieldType type, String field, Object fieldValue) {
        if (fieldValue == null) {
            throw new SchemaViolationException("null value for field: " + field);
        }
        boolean matches = switch (type) {
            case STRING -> fieldValue instanceof String;
            case BOOLEAN -> fieldValue instanceof Boolean;
            case INTEGER -> fieldValue instanceof Integer;
        };
        if (!matches) {
            throw new SchemaViolationException("field " + field + " must be of type " + type);
        }
    }
}
