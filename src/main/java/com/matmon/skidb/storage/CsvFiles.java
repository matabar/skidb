package com.matmon.skidb.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.matmon.skidb.exception.StorageException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CsvFiles {

    private static final CsvMapper CSV = new CsvMapper();

    static void writeSchema(Path file, Schema schema) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (Map.Entry<String, FieldType> entry : new TreeMap<>(schema.getFieldTypes()).entrySet()) {
            rows.add(Map.of("field", entry.getKey(), "type", entry.getValue().name()));
        }
        writeRows(file, List.of("field", "type"), rows);
    }

    static void writeIndex(Path file, List<Map<String, String>> rows) {
        writeRows(file, List.of("value", "key"), rows);
    }

    static Schema readSchema(Path file) {
        Map<String, FieldType> fieldTypes = new HashMap<>();
        for (Map<String, String> row : readRows(file)) {
            fieldTypes.put(row.get("field"), FieldType.valueOf(row.get("type")));
        }
        return new Schema(fieldTypes);
    }

    static void writeDocument(Path file, Schema schema, Map<String, Object> document) {
        List<String> columns = new ArrayList<>(new TreeSet<>(schema.getFieldTypes().keySet()));
        CsvSchema.Builder builder = CsvSchema.builder();
        columns.forEach(builder::addColumn);
        CsvSchema csvSchema = builder.build().withHeader();
        Map<String, String> row = new HashMap<>();
        for (String column : columns) {
            row.put(column, String.valueOf(document.get(column)));
        }
        try (SequenceWriter writer = CSV.writer(csvSchema).writeValues(file.toFile())) {
            writer.write(row);
        } catch (IOException e) {
            throw new StorageException("cannot write " + file + ": " + e.getMessage());
        }
    }

    static Map<String, Object> readDocument(Path file, Schema schema) {
        Map<String, Object> document = new HashMap<>();
        for (Map.Entry<String, String> entry : readRows(file).get(0).entrySet()) {
            FieldType type = schema.getFieldTypes().get(entry.getKey());
            document.put(entry.getKey(), convert(type, entry.getValue()));
        }
        return document;
    }

    static void ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new StorageException("cannot create " + directory + ": " + e.getMessage());
        }
    }

    static void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("cannot delete " + file + ": " + e.getMessage());
        }
    }

    private static void writeRows(Path file, List<String> columns, List<Map<String, String>> rows) {
        CsvSchema.Builder builder = CsvSchema.builder();
        columns.forEach(builder::addColumn);
        CsvSchema csvSchema = builder.build().withHeader();
        try (SequenceWriter writer = CSV.writer(csvSchema).writeValues(file.toFile())) {
            writer.writeAll(rows);
        } catch (IOException e) {
            throw new StorageException("cannot write " + file + ": " + e.getMessage());
        }
    }

    private static List<Map<String, String>> readRows(Path file) {
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<Map<String, String>> rows = CSV.readerFor(new TypeReference<Map<String, String>>() {})
                .with(csvSchema)
                .readValues(file.toFile())) {
            return rows.readAll();
        } catch (IOException e) {
            throw new StorageException("cannot read " + file + ": " + e.getMessage());
        }
    }

    private static Object convert(FieldType type, String value) {
        return switch (type) {
            case STRING -> value;
            case BOOLEAN -> Boolean.valueOf(value);
            case INTEGER -> Integer.valueOf(value);
        };
    }
}
