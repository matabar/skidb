package com.matmon.skidb.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvDatabaseTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    @TempDir
    Path baseDirectory;

    @Test
    void createCollectionWritesSchemaFile() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));

        database.createCollection("employees", STUDENT);

        assertTrue(Files.exists(baseDirectory.resolve("employees/schema.csv")));
    }

    @Test
    void collectionSurvivesReopen() {
        new CsvDatabase(TestConfig.at(baseDirectory)).createCollection("employees", STUDENT);

        CsvDatabase reopened = new CsvDatabase(TestConfig.at(baseDirectory));

        assertEquals(Set.of("employees"), reopened.collectionNames());
        assertEquals(STUDENT.getFieldTypes(), reopened.collection("employees").getSchema().getFieldTypes());
    }

    @Test
    void dropRemovesCollectionFolder() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        database.createCollection("employees", STUDENT);

        database.dropCollection("employees");

        assertFalse(Files.exists(baseDirectory.resolve("employees")));
        assertEquals(Set.of(), new CsvDatabase(TestConfig.at(baseDirectory)).collectionNames());
    }
}
