package com.matmon.skidb.storage;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.model.Schema;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CsvCollection implements Collection {

    private final Collection delegate;
    private final Path directory;
    private final String documentsFolder;
    private final String indexesFolder;
    private final Set<String> indexedFields;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Schema getSchema() {
        return delegate.getSchema();
    }

    @Override
    public void insert(String key, Map<String, Object> document) {
        delegate.insert(key, document);
        saveDocument(key);
        saveIndexes();
    }

    @Override
    public Map<String, Object> find(String key) {
        return delegate.find(key);
    }

    @Override
    public void update(String key, Map<String, Object> changes) {
        delegate.update(key, changes);
        saveDocument(key);
        saveIndexes();
    }

    @Override
    public void delete(String key) {
        delegate.delete(key);
        CsvFiles.deleteFile(documentFile(key));
        saveIndexes();
    }

    @Override
    public Map<String, Map<String, Object>> query(Map<String, Object> query) {
        return delegate.query(query);
    }

    @Override
    public void createIndex(String field) {
        delegate.createIndex(field);
        indexedFields.add(field);
        saveIndexes();
    }

    Path documentFile(String key) {
        return directory.resolve(documentsFolder).resolve(key + ".csv");
    }

    private void saveDocument(String key) {
        CsvFiles.ensureDirectory(directory.resolve(documentsFolder));
        CsvFiles.writeDocument(documentFile(key), getSchema(), delegate.find(key));
    }

    private void saveIndexes() {
        if (indexedFields.isEmpty()) {
            return;
        }
        CsvFiles.ensureDirectory(directory.resolve(indexesFolder));
        Map<String, Map<String, Object>> documents = delegate.query(Map.of());
        for (String field : indexedFields) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : documents.entrySet()) {
                rows.add(Map.of("value", String.valueOf(entry.getValue().get(field)), "key", entry.getKey()));
            }
            rows.sort(Comparator.comparing((Map<String, String> row) -> row.get("value"))
                    .thenComparing(row -> row.get("key")));
            CsvFiles.writeIndex(directory.resolve(indexesFolder).resolve(field + ".csv"), rows);
        }
    }
}
