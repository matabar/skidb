package com.matmon.skidb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.matmon.skidb.api.Collection;
import com.matmon.skidb.exception.CollectionExistsException;
import com.matmon.skidb.exception.CollectionNotFoundException;
import com.matmon.skidb.model.FieldType;
import com.matmon.skidb.model.Schema;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InMemoryDatabaseTest {

    private static final Schema STUDENT = new Schema(Map.of("firstName", FieldType.STRING));

    private final InMemoryDatabase database = new InMemoryDatabase();

    @Test
    void createdCollectionIsReturnedByName() {
        Collection created = database.createCollection("students", STUDENT);

        assertEquals(created, database.collection("students"));
        assertEquals(Set.of("students"), database.collectionNames());
    }

    @Test
    void createRejectsDuplicateName() {
        database.createCollection("students", STUDENT);

        assertThrows(CollectionExistsException.class,
                () -> database.createCollection("students", STUDENT));
    }

    @Test
    void lookupUnknownCollectionThrows() {
        assertThrows(CollectionNotFoundException.class, () -> database.collection("missing"));
    }

    @Test
    void dropRemovesCollection() {
        database.createCollection("students", STUDENT);

        database.dropCollection("students");

        assertThrows(CollectionNotFoundException.class, () -> database.collection("students"));
        assertEquals(Set.of(), database.collectionNames());
    }

    @Test
    void dropUnknownCollectionThrows() {
        assertThrows(CollectionNotFoundException.class, () -> database.dropCollection("missing"));
    }
}
