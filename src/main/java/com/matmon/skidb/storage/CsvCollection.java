package com.matmon.skidb.storage;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.model.Schema;
import java.nio.file.Path;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CsvCollection implements Collection {

    private final Collection delegate;
    private final Path directory;

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
    }

    @Override
    public Map<String, Object> find(String key) {
        return delegate.find(key);
    }

    @Override
    public void update(String key, Map<String, Object> changes) {
        delegate.update(key, changes);
    }

    @Override
    public void delete(String key) {
        delegate.delete(key);
    }

    @Override
    public Map<String, Map<String, Object>> query(Map<String, Object> query) {
        return delegate.query(query);
    }

    @Override
    public void createIndex(String field) {
        delegate.createIndex(field);
    }
}
