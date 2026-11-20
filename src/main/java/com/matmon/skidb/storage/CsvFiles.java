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
import java.util.ArrayList;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CsvFiles {

    private static final CsvMapper CSV = new CsvMapper();

    static void writeSchema(Path file, Schema schema) {
        CsvSchema csvSchema = CsvSchema.builder()
                .addColumn("field")
                .addColumn("type")
                .build()
                .withHeader();
        List<Map<String, String>> rows = new ArrayList<>();
        for (Map.Entry<String, FieldType> entry : new TreeMap<>(schema.getFieldTypes()).entrySet()) {
            rows.add(Map.of("field", entry.getKey(), "type", entry.getValue().name()));
        }
        try (SequenceWriter writer = CSV.writer(csvSchema).writeValues(file.toFile())) {
            writer.writeAll(rows);
        } catch (IOException e) {
            throw new StorageException("cannot write " + file + ": " + e.getMessage());
        }
    }

    static Schema readSchema(Path file) {
        Map<String, FieldType> fieldTypes = new HashMap<>();
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<Map<String, String>> rows = CSV.readerFor(new TypeReference<Map<String, String>>() {})
                .with(csvSchema)
                .readValues(file.toFile())) {
            while (rows.hasNext()) {
                Map<String, String> row = rows.next();
                fieldTypes.put(row.get("field"), FieldType.valueOf(row.get("type")));
            }
        } catch (IOException e) {
            throw new StorageException("cannot read " + file + ": " + e.getMessage());
        }
        return new Schema(fieldTypes);
    }
}
