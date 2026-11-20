package com.matmon.skidb.api;

import com.matmon.skidb.model.Schema;
import java.util.Set;

public interface Database {

    Collection createCollection(String name, Schema schema);

    Collection collection(String name);

    void dropCollection(String name);

    Set<String> collectionNames();
}
