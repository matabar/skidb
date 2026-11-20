package com.matmon.skidb.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.exception.ConfigException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsBaseDirectory() throws Exception {
        Path file = write("base-directory: data\n");

        assertEquals("data", ConfigLoader.load(file).getBaseDirectory());
    }

    @Test
    void missingFileThrows() {
        assertThrows(ConfigException.class, () -> ConfigLoader.load(tempDir.resolve("missing.yaml")));
    }

    @Test
    void missingBaseDirectoryThrows() throws Exception {
        Path file = write("{}\n");

        assertThrows(ConfigException.class, () -> ConfigLoader.load(file));
    }

    @Test
    void blankBaseDirectoryThrows() throws Exception {
        Path file = write("base-directory: \"\"\n");

        assertThrows(ConfigException.class, () -> ConfigLoader.load(file));
    }

    private Path write(String content) throws Exception {
        Path file = tempDir.resolve("config.yaml");
        Files.writeString(file, content);
        return file;
    }
}
