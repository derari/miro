package org.cthul.miro.query.parts;

public abstract class AbstractQueryPart implements QueryPart {

    private final String key;

    public AbstractQueryPart(String key) {
        this.key = key;
    }
    
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void put(String key, Object... args) {
        if ((key == null || key.isEmpty()) && 
                (args == null || args.length == 0)) {
            return;
        }
        throw new IllegalArgumentException("Invalid key: " + key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + getKey();
    }
}
