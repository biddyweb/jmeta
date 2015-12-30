package org.meta.storage;

import java.util.NavigableSet;

public interface CollectionStorage {

    /**
     * TODO
     */
    public <T> NavigableSet<T> getPersistentTreeSet(String name);
}
