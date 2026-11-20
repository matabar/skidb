package com.matmon.skidb.engine;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.exception.DuplicateKeyException;
import com.matmon.skidb.exception.KeyNotFoundException;
import com.matmon.skidb.exception.UnknownFieldException;
import com.matmon.skidb.model.Schema;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InMemoryCollection implements Collection {

    @Getter
    private final String name;
    @Getter
    private final Schema schema;
    private final Map<String, Map<String, Object>> documents = new HashMap<>();
    private final Map<String, Map<Object, Set<String>>> indexes = new HashMap<>();

    @Override
    public void insert(String key, Map<String, Object> document) {
        if (documents.containsKey(key)) {
            throw new DuplicateKeyException("duplicate key: " + key);
        }
        SchemaValidator.validateDocument(schema, document);
        Map<String, Object> stored = Map.copyOf(document);
        documents.put(key, stored);
        addToIndex(key, stored);
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
        Map<String, Object> existing = find(key);
        Map<String, Object> merged = new HashMap<>(existing);
        merged.putAll(changes);
        SchemaValidator.validateDocument(schema, merged);
        Map<String, Object> stored = Map.copyOf(merged);
        removeFromIndex(key, existing);
        documents.put(key, stored);
        addToIndex(key, stored);
    }

    @Override
    public void delete(String key) {
        if (!documents.containsKey(key)) {
            throw new KeyNotFoundException("key not found: " + key);
        }
        removeFromIndex(key, documents.remove(key));
    }

    @Override
    public Map<String, Map<String, Object>> query(Map<String, Object> query) {
        SchemaValidator.validateQuery(schema, query);
        Map<String, Map<String, Object>> matches = new HashMap<>();
        for (String key : keysToCheck(query)) {
            Map<String, Object> document = documents.get(key);
            if (matchesQuery(document, query)) {
                matches.put(key, document);
            }
        }
        return Map.copyOf(matches);
    }

    @Override
    public void createIndex(String field) {
        if (!schema.getFieldTypes().containsKey(field)) {
            throw new UnknownFieldException("unknown field: " + field);
        }
        if (indexes.containsKey(field)) {
            return;
        }
        Map<Object, Set<String>> index = new HashMap<>();
        indexes.put(field, index);
        for (Map.Entry<String, Map<String, Object>> entry : documents.entrySet()) {
            index.computeIfAbsent(entry.getValue().get(field), value -> new HashSet<>())
                    .add(entry.getKey());
        }
    }

    private Set<String> keysToCheck(Map<String, Object> query) {
        for (Map.Entry<String, Object> entry : query.entrySet()) {
            Map<Object, Set<String>> index = indexes.get(entry.getKey());
            if (index != null) {
                return index.getOrDefault(entry.getValue(), Set.of());
            }
        }
        return documents.keySet();
    }

    private void addToIndex(String key, Map<String, Object> document) {
        for (Map.Entry<String, Map<Object, Set<String>>> index : indexes.entrySet()) {
            index.getValue()
                    .computeIfAbsent(document.get(index.getKey()), value -> new HashSet<>())
                    .add(key);
        }
    }

    private void removeFromIndex(String key, Map<String, Object> document) {
        for (Map.Entry<String, Map<Object, Set<String>>> index : indexes.entrySet()) {
            Object value = document.get(index.getKey());
            Set<String> keys = index.getValue().get(value);
            if (keys != null) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    index.getValue().remove(value);
                }
            }
        }
    }

    private static boolean matchesQuery(Map<String, Object> document, Map<String, Object> query) {
        return query.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(document.get(entry.getKey())));
    }
}
