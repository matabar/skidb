package com.matmon.skidb.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvIndexTest {

    private static final Schema STUDENT = new Schema(Map.of(
            "firstName", FieldType.STRING,
            "lastName", FieldType.STRING));

    @TempDir
    Path baseDirectory;

    @Test
    void createIndexWritesIndexFile() throws Exception {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        Collection students = database.createCollection("students", STUDENT);
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));

        students.createIndex("firstName");

        Path indexFile = baseDirectory.resolve("students/indexes/firstName.csv");
        assertTrue(Files.readString(indexFile).contains("Noa,1"));
    }

    @Test
    void indexFileFollowsInserts() throws Exception {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        Collection students = database.createCollection("students", STUDENT);
        students.createIndex("firstName");

        students.insert("2", Map.of("firstName", "Itai", "lastName", "Levi"));

        Path indexFile = baseDirectory.resolve("students/indexes/firstName.csv");
        assertTrue(Files.readString(indexFile).contains("Itai,2"));
    }

    @Test
    void indexSurvivesReopen() {
        CsvDatabase database = new CsvDatabase(TestConfig.at(baseDirectory));
        Collection students = database.createCollection("students", STUDENT);
        students.insert("1", Map.of("firstName", "Noa", "lastName", "Cohen"));
        students.createIndex("firstName");

        Collection reopened = new CsvDatabase(TestConfig.at(baseDirectory)).collection("students");

        assertEquals(1, reopened.query(Map.of("firstName", "Noa")).size());
    }
}
