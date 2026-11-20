package com.matmon.skidb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.UnknownFieldException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CollectionIndexTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    private final InMemoryCollection students = new InMemoryCollection("students", STUDENT);

    @Test
    void indexBuiltFromExistingDocumentsStillMatchesAllFields() {
        students.insert("1", Map.of("firstName", "Israel", "lastName", "Israeli"));
        students.insert("2", Map.of("firstName", "Israel", "lastName", "Cohen"));
        students.insert("3", Map.of("firstName", "Noa", "lastName", "Levi"));

        students.createIndex("firstName");

        assertEquals(2, students.query(Map.of("firstName", "Israel")).size());
        assertEquals(1, students.query(Map.of("firstName", "Israel", "lastName", "Cohen")).size());
    }

    @Test
    void indexSeesDocumentsInsertedAfterCreation() {
        students.createIndex("firstName");

        students.insert("1", Map.of("firstName", "Israel", "lastName", "Israeli"));

        assertEquals(1, students.query(Map.of("firstName", "Israel")).size());
    }

    @Test
    void indexFollowsUpdates() {
        students.insert("1", Map.of("firstName", "Israel", "lastName", "Israeli"));
        students.createIndex("firstName");

        students.update("1", Map.of("firstName", "Noa"));

        assertEquals(0, students.query(Map.of("firstName", "Israel")).size());
        assertEquals(1, students.query(Map.of("firstName", "Noa")).size());
    }

    @Test
    void indexFollowsDeletes() {
        students.insert("1", Map.of("firstName", "Israel", "lastName", "Israeli"));
        students.createIndex("firstName");

        students.delete("1");

        assertEquals(0, students.query(Map.of("firstName", "Israel")).size());
    }

    @Test
    void createIndexRejectsUnknownField() {
        assertThrows(UnknownFieldException.class, () -> students.createIndex("nickname"));
    }
}
