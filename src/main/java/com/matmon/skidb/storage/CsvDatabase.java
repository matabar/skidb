package com.matmon.skidb.storage;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.api.Database;
import com.matmon.skidb.config.Config;
import com.matmon.skidb.engine.InMemoryDatabase;
import com.matmon.skidb.exception.CollectionNotFoundException;
import com.matmon.skidb.exception.StorageException;
import com.matmon.skidb.model.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class CsvDatabase implements Database {

    private final Path root;
    private final String schemaFile;
    private final InMemoryDatabase memory = new InMemoryDatabase();
    private final Map<String, CsvCollection> collections = new HashMap<>();

    public CsvDatabase(Config config) {
        this.root = Path.of(config.getBaseDirectory());
        this.schemaFile = config.getSchemaFile();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("cannot create " + root + ": " + e.getMessage());
        }
        load();
    }

    @Override
    public Collection createCollection(String name, Schema schema) {
        Collection inner = memory.createCollection(name, schema);
        Path directory = root.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new StorageException("cannot create " + directory + ": " + e.getMessage());
        }
        CsvFiles.writeSchema(directory.resolve(schemaFile), schema);
        CsvCollection collection = new CsvCollection(inner, directory);
        collections.put(name, collection);
        return collection;
    }

    @Override
    public Collection collection(String name) {
        CsvCollection collection = collections.get(name);
        if (collection == null) {
            throw new CollectionNotFoundException("collection not found: " + name);
        }
        return collection;
    }

    @Override
    public void dropCollection(String name) {
        CsvCollection collection = collections.get(name);
        if (collection == null) {
            throw new CollectionNotFoundException("collection not found: " + name);
        }
        memory.dropCollection(name);
        deleteRecursively(root.resolve(name));
        collections.remove(name);
    }

    @Override
    public Set<String> collectionNames() {
        return memory.collectionNames();
    }

    private void load() {
        try (Stream<Path> folders = Files.list(root)) {
            for (Path folder : folders.sorted().toList()) {
                Path schemaFilePath = folder.resolve(schemaFile);
                if (Files.isDirectory(folder) && Files.exists(schemaFilePath)) {
                    String name = folder.getFileName().toString();
                    Schema schema = CsvFiles.readSchema(schemaFilePath);
                    Collection inner = memory.createCollection(name, schema);
                    collections.put(name, new CsvCollection(inner, folder));
                }
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + root + ": " + e.getMessage());
        }
    }

    private static void deleteRecursively(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            throw new StorageException("cannot delete " + directory + ": " + e.getMessage());
        }
    }
}
