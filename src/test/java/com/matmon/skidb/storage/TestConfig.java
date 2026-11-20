package com.matmon.skidb.storage;

import com.matmon.skidb.config.Config;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TestConfig {

    static Config at(Path baseDirectory) {
        Config config = new Config();
        config.setBaseDirectory(baseDirectory.toString());
        config.setSchemaFile("schema.csv");
        config.setDocumentsFolder("records");
        config.setIndexesFolder("indexes");
        return config;
    }
}
