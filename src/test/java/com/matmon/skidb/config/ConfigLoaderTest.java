package com.matmon.skidb.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.ConfigException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigLoaderTest {

    private static final String ALL_SETTINGS = """
            base-directory: data
            schema-file: schema.csv
            documents-folder: records
            """;

    @TempDir
    Path tempDir;

    @Test
    void loadsAllSettings() throws Exception {
        Config config = ConfigLoader.load(write(ALL_SETTINGS));

        assertEquals("data", config.getBaseDirectory());
        assertEquals("schema.csv", config.getSchemaFile());
        assertEquals("records", config.getDocumentsFolder());
    }

    @Test
    void missingFileThrows() {
        assertThrows(ConfigException.class, () -> ConfigLoader.load(tempDir.resolve("missing.yaml")));
    }

    @Test
    void missingSettingThrows() throws Exception {
        Path file = write("{}\n");

        assertThrows(ConfigException.class, () -> ConfigLoader.load(file));
    }

    @Test
    void blankSettingThrows() throws Exception {
        Path file = write(ALL_SETTINGS.replace("documents-folder: records", "documents-folder: \"\""));

        assertThrows(ConfigException.class, () -> ConfigLoader.load(file));
    }

    private Path write(String content) throws Exception {
        Path file = tempDir.resolve("config.yaml");
        Files.writeString(file, content);
        return file;
    }
}
