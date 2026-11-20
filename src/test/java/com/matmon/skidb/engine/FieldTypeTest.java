package com.matmon.skidb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.SchemaViolationException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FieldTypeTest {

    private static final Schema COURSE = new Schema(Map.of(
            "name", FieldType.STRING,
            "active", FieldType.BOOLEAN,
            "credits", FieldType.INTEGER));

    private final InMemoryCollection courses = new InMemoryCollection("courses", COURSE);

    @Test
    void insertAcceptsBooleanAndIntegerFields() {
        courses.insert("1", Map.of("name", "Algorithms", "active", true, "credits", 4));

        assertEquals(true, courses.find("1").get("active"));
        assertEquals(4, courses.find("1").get("credits"));
    }

    @Test
    void insertRejectsStringForBoolean() {
        assertThrows(SchemaViolationException.class,
                () -> courses.insert("1", Map.of("name", "Algorithms", "active", "yes", "credits", 4)));
    }

    @Test
    void insertRejectsStringForInteger() {
        assertThrows(SchemaViolationException.class,
                () -> courses.insert("1", Map.of("name", "Algorithms", "active", true, "credits", "4")));
    }

    @Test
    void updateRejectsStringForInteger() {
        courses.insert("1", Map.of("name", "Algorithms", "active", true, "credits", 4));

        assertThrows(SchemaViolationException.class,
                () -> courses.update("1", Map.of("credits", "4")));
    }
}
