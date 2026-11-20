package com.matmon.skidb.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvDocumentTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    private static final Schema COURSE = new Schema(Map.of(
            "name", FieldType.STRING,
            "active", FieldType.BOOLEAN,
            "credits", FieldType.INTEGER));

    @TempDir
    Path baseDirectory;

    @Test
    void insertWritesDocumentFile() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        database.createCollection("students", STUDENT).insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        assertTrue(Files.exists(baseDirectory.resolve("students/records/1.csv")));
    }

    @Test
    void documentSurvivesReopen() {
        new CsvDatabase(TestConfig.at(baseDirectory)).createCollection("students", STUDENT)
                .insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        CsvDatabase reopened = new CsvDatabase(TestConfig.at(baseDirectory));

        assertEquals(Map.of("firstName", "Noa", "lastName", "Cohen"), reopened.collection("students").find("1"));
    }

    @Test
    void updateIsSavedAndSurvivesReopen() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        database.createCollection("students", STUDENT).insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        database.collection("students").update("1", Map.of("firstName", "Itai"));

        assertEquals(Map.of("firstName", "Itai", "lastName", "Cohen"),
                new CsvDatabase(TestConfig.at(baseDirectory)).collection("students").find("1"));
    }

    @Test
    void deleteRemovesDocumentFile() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        database.createCollection("students", STUDENT).insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        database.collection("students").delete("1");

        assertFalse(Files.exists(baseDirectory.resolve("students/records/1.csv")));
        assertEquals(0, new CsvDatabase(TestConfig.at(baseDirectory)).collection("students").query(Map.of()).size());
    }

    @Test
    void booleanAndIntegerValuesKeepTheirTypes() {
        new CsvDatabase(TestConfig.at(baseDirectory)).createCollection("courses", COURSE)
                .insert("1", Map.of("name", "Algorithms", "active", true, "credits", 4));

        Map<String, Object> reloaded = new CsvDatabase(TestConfig.at(baseDirectory)).collection("courses").find("1");

        assertEquals(true, reloaded.get("active"));
        assertEquals(4, reloaded.get("credits"));
    }
}
