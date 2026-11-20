package com.matmon.skidb.engine;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.api.Database;
import com.matmon.skidb.exception.CollectionExistsException;
import com.matmon.skidb.exception.CollectionNotFoundException;
import com.matmon.skidb.model.Schema;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class InMemoryDatabase implements Database {

    private final Map<String, Collection> collections = new HashMap<>();

    @Override
    public Collection createCollection(String name, Schema schema) {
        if (collections.containsKey(name)) {
            throw new CollectionExistsException("collection already exists: " + name);
        }
        Collection collection = new InMemoryCollection(name, schema);
        collections.put(name, collection);
        return collection;
    }

    @Override
    public Collection collection(String name) {
        Collection collection = collections.get(name);
        if (collection == null) {
            throw new CollectionNotFoundException("collection not found: " + name);
        }
        return collection;
    }

    @Override
    public void dropCollection(String name) {
        if (!collections.containsKey(name)) {
            throw new CollectionNotFoundException("collection not found: " + name);
        }
        collections.remove(name);
    }

    @Override
    public Set<String> collectionNames() {
        return Set.copyOf(collections.keySet());
    }
}
