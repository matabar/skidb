package com.matmon.skidb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.matmon.skidb.exception.ConfigException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static Config load(Path file) {
        if (!Files.exists(file)) {
            throw new ConfigException("config file not found: " + file);
        }
        Config config;
        try {
            config = YAML_MAPPER.readValue(file.toFile(), Config.class);
        } catch (IOException e) {
            throw new ConfigException("cannot read config file " + file + ": " + e.getMessage());
        }
        if (config == null || config.getBaseDirectory() == null || config.getBaseDirectory().isBlank()) {
            throw new ConfigException("base-directory must be set in " + file);
        }
        return config;
    }
}
