package org.cthul.miro.query.parts;

public abstract class AbstractQueryPart implements QueryPart {

    private final Object key;

    public AbstractQueryPart(Object key) {
        this.key = key;
    }
    
    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public void put(Object key, Object... args) {
        if (key == null && 
                (args == null || args.length == 0)) {
            return;
        }
        throw new IllegalArgumentException(getKey() + ": invalid key: " + key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getKey();
    }
}
