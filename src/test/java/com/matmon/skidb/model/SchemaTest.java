package com.matmon.skidb.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SchemaTest {

    @Test
    void keepsFieldTypes() {
        Schema schema = new Schema(Map.of("firstName", FieldType.STRING));

        assertEquals(FieldType.STRING, schema.getFieldTypes().get("firstName"));
    }

    @Test
    void copiesFieldsSoLaterChangesDoNotLeakIn() {
        Map<String, FieldType> source = new HashMap<>();
        source.put("firstName", FieldType.STRING);
        Schema schema = new Schema(source);

        source.put("lastName", FieldType.STRING);

        assertEquals(1, schema.getFieldTypes().size());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new Schema(null));
    }
}
