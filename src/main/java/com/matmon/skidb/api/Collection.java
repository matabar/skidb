package com.matmon.skidb.api;

import com.matmon.skidb.model.Schema;
import java.util.Map;

public interface Collection {

    String getName();

    Schema getSchema();

    void insert(String key, Map<String, Object> document);

    Map<String, Object> find(String key);

    void update(String key, Map<String, Object> changes);

    void delete(String key);
}
