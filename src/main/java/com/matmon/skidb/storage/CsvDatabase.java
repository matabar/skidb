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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class CsvDatabase implements Database {

    private final Path root;
    private final String schemaFile;
    private final String documentsFolder;
    private final String indexesFolder;
    private final InMemoryDatabase memory = new InMemoryDatabase();
    private final Map<String, CsvCollection> collections = new HashMap<>();

    public CsvDatabase(Config config) {
        this.root = Path.of(config.getBaseDirectory());
        this.schemaFile = config.getSchemaFile();
        this.documentsFolder = config.getDocumentsFolder();
        this.indexesFolder = config.getIndexesFolder();
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
        CsvCollection collection = new CsvCollection(inner, directory, documentsFolder, indexesFolder, new HashSet<>());
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
                    loadDocuments(folder, inner, schema);
                    Set<String> indexedFields = loadIndexes(folder, inner);
                    collections.put(name, new CsvCollection(inner, folder, documentsFolder, indexesFolder, indexedFields));
                }
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + root + ": " + e.getMessage());
        }
    }

    private Set<String> loadIndexes(Path folder, Collection inner) {
        Set<String> indexedFields = new HashSet<>();
        Path indexes = folder.resolve(indexesFolder);
        if (!Files.isDirectory(indexes)) {
            return indexedFields;
        }
        try (Stream<Path> files = Files.list(indexes)) {
            for (Path file : files.sorted().toList()) {
                String fileName = file.getFileName().toString();
                String field = fileName.substring(0, fileName.length() - ".csv".length());
                inner.createIndex(field);
                indexedFields.add(field);
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + indexes + ": " + e.getMessage());
        }
        return indexedFields;
    }

    private void loadDocuments(Path folder, Collection inner, Schema schema) {
        Path documents = folder.resolve(documentsFolder);
        if (!Files.isDirectory(documents)) {
            return;
        }
        try (Stream<Path> files = Files.list(documents)) {
            for (Path file : files.sorted().toList()) {
                String fileName = file.getFileName().toString();
                String key = fileName.substring(0, fileName.length() - ".csv".length());
                inner.insert(key, CsvFiles.readDocument(file, schema));
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + documents + ": " + e.getMessage());
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
