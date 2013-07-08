package org.cthul.miro.util;

import org.cthul.miro.MiConnection;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class MappedQuery<Entity> extends MappedStatement<Entity> {

    private final String query;
    private final String[] selectedFields;
    private Object[] arguments = null;

    public MappedQuery(MiConnection cnn, Mapping<Entity> mapping, String query, String[] selectedFields, Object... arguments) {
        super(cnn, mapping);
        this.query = query;
        this.selectedFields = selectedFields;
        this.arguments = arguments;
    }

    public MappedQuery(MiConnection cnn, Mapping<Entity> mapping, String query, String[] selectedFields) {
        super(cnn, mapping);
        this.query = query;
        this.selectedFields = selectedFields;
    }

    @Override
    public void put(String key, String subKey, Object... args) {
        if (!key.isEmpty()) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }
        if (subKey != null) {
            throw new IllegalArgumentException("Unknown sub-key: " + subKey);
        }
        arguments = args;
    }

    @Override
    protected String[] selectedFields() {
        return selectedFields;
    }

    @Override
    protected String queryString() {
        return query;
    }

    @Override
    protected Object[] arguments() {
        return arguments;
    }
}
