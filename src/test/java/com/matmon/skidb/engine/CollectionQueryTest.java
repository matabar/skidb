package com.matmon.skidb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.SchemaViolationException;
import com.matmon.skidb.exception.UnknownFieldException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CollectionQueryTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    private final InMemoryCollection students = new InMemoryCollection("students", STUDENT);

    private void addSampleStudents() {
        students.insert("1", Map.of("firstName", "Israel", "lastName", "Israeli"));
        students.insert("2", Map.of("firstName", "Israel", "lastName", "Cohen"));
        students.insert("3", Map.of("firstName", "Noa", "lastName", "Levi"));
    }

    @Test
    void queryReturnsAllDocumentsMatchingOneField() {
        addSampleStudents();

        assertEquals(2, students.query(Map.of("firstName", "Israel")).size());
    }

    @Test
    void queryRequiresEveryFieldToMatch() {
        addSampleStudents();

        Map<String, Map<String, Object>> matches =
                students.query(Map.of("firstName", "Israel", "lastName", "Cohen"));

        assertEquals(Map.of("firstName", "Israel", "lastName", "Cohen"), matches.get("2"));
        assertEquals(1, matches.size());
    }

    @Test
    void queryWithNoMatchReturnsEmpty() {
        addSampleStudents();

        assertEquals(0, students.query(Map.of("firstName", "Dana")).size());
    }

    @Test
    void queryWithEmptyQueryReturnsAllDocuments() {
        addSampleStudents();

        assertEquals(3, students.query(Map.of()).size());
    }

    @Test
    void queryRejectsUnknownField() {
        assertThrows(UnknownFieldException.class,
                () -> students.query(Map.of("nickname", "Nuni")));
    }

    @Test
    void queryRejectsWrongType() {
        assertThrows(SchemaViolationException.class,
                () -> students.query(Map.of("firstName", 5)));
    }
}
