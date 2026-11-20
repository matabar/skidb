package com.matmon.skidb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.DuplicateKeyException;
import com.matmon.skidb.exception.KeyNotFoundException;
import com.matmon.skidb.exception.SchemaViolationException;
import com.matmon.skidb.exception.UnknownFieldException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryCollectionTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    private final InMemoryCollection students = new InMemoryCollection("students", STUDENT);

    @Test
    void insertThenFind() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertEquals("Noa", students.find("1").get("firstName"));
    }

    @Test
    void insertRejectsDuplicateKey() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertThrows(DuplicateKeyException.class,
                () -> students.insert("1", Map.of("firstName", "Itai", "lastName", "Levi")));
    }

    @Test
    void insertRejectsMissingField() {
        assertThrows(SchemaViolationException.class,
                () -> students.insert("1", Map.of("firstName", "Noa")));
    }

    @Test
    void insertRejectsUnknownField() {
        assertThrows(UnknownFieldException.class,
                () -> students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen", "nickname", "Nuni")));
    }

    @Test
    void insertRejectsNonStringValue() {
        assertThrows(SchemaViolationException.class,
                () -> students.insert("1", Map.of("firstName", "Noa", "lastName", 36)));
    }

    @Test
    void findMissingKeyThrows() {
        assertThrows(KeyNotFoundException.class, () -> students.find("missing"));
    }

    @Test
    void updateChangesOnlyTheSentField() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        students.update("1", Map.of("firstName", "Tamar"));

        assertEquals("Tamar", students.find("1").get("firstName"));
        assertEquals("Cohen", students.find("1").get("lastName"));
    }

    @Test
    void updateRejectsUnknownField() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertThrows(UnknownFieldException.class,
                () -> students.update("1", Map.of("nickname", "Nuni")));
    }

    @Test
    void updateRejectsNonStringValue() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertThrows(SchemaViolationException.class,
                () -> students.update("1", Map.of("firstName", 36)));
    }

    @Test
    void updateMissingKeyThrows() {
        assertThrows(KeyNotFoundException.class,
                () -> students.update("missing", Map.of("firstName", "Noa")));
    }

    @Test
    void failedUpdateLeavesDocumentUnchanged() {
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertThrows(SchemaViolationException.class,
                () -> students.update("1", Map.of("firstName", 36)));

        assertEquals("Noa", students.find("1").get("firstName"));
    }
}
