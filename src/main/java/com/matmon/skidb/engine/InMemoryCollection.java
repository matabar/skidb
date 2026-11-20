package com.matmon.skidb.engine;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.exception.DuplicateKeyException;
import com.matmon.skidb.exception.KeyNotFoundException;
import com.matmon.skidb.model.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InMemoryCollection implements Collection {

    @Getter
    private final String name;
    @Getter
    private final Schema schema;
    private final Map<String, Map<String, Object>> documents = new HashMap<>();

    @Override
    public void insert(String key, Map<String, Object> document) {
        if (documents.containsKey(key)) {
            throw new DuplicateKeyException("duplicate key: " + key);
        }
        SchemaValidator.validateDocument(schema, document);
        documents.put(key, Map.copyOf(document));
    }

    @Override
    public Map<String, Object> find(String key) {
        Map<String, Object> document = documents.get(key);
        if (document == null) {
            throw new KeyNotFoundException("key not found: " + key);
        }
        return document;
    }

    @Override
    public void update(String key, Map<String, Object> changes) {
        Map<String, Object> merged = new HashMap<>(find(key));
        merged.putAll(changes);
        SchemaValidator.validateDocument(schema, merged);
        documents.put(key, Map.copyOf(merged));
    }

    @Override
    public void delete(String key) {
        if (!documents.containsKey(key)) {
            throw new KeyNotFoundException("key not found: " + key);
        }
        documents.remove(key);
    }

    @Override
    public Map<String, Map<String, Object>> query(Map<String, Object> query) {
        SchemaValidator.validateQuery(schema, query);
        Map<String, Map<String, Object>> matches = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : documents.entrySet()) {
            if (matchesQuery(entry.getValue(), query)) {
                matches.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(matches);
    }

    private static boolean matchesQuery(Map<String, Object> document, Map<String, Object> query) {
        return query.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(document.get(entry.getKey())));
    }
}
