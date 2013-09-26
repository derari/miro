package org.cthul.miro.map;

import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;

/**
 *
 */
public class MappedQueryString<Entity> extends AbstractMappedStatement<Entity> {
    
    private final List<String> fields;
    private final String query;
    private Object[] arguments;

    public MappedQueryString(MiConnection cnn, Mapping<Entity> mapping, List<String> fields, String query, Object... arguments) {
        super(cnn, mapping);
        this.fields = fields;
        this.query = query;
        this.arguments = arguments;
    }
    
    public MappedQueryString(MiConnection cnn, Mapping<Entity> mapping, List<String> fields, String query) {
        super(cnn, mapping);
        this.fields = fields;
        this.query = query;
        this.arguments = null;
    }
    
    public MappedQueryString(MiConnection cnn, Mapping<Entity> mapping, String query, String... fields) {
        super(cnn, mapping);
        this.fields = Arrays.asList(fields);
        this.query = query;
        this.arguments = null;
    }

    @Override
    protected List<String> selectedFields() {
        return fields;
    }

    @Override
    protected String queryString() {
        return query;
    }

    @Override
    protected Object[] arguments() {
        return arguments;
    }

    @Override
    public void put(String key, String subKey, Object... args) {
        if (key != null && !key.isEmpty()) {
            throw new IllegalArgumentException("No key expected: " + key);
        }
        if (subKey != null && !subKey.isEmpty()) {
            throw new IllegalArgumentException("No sub-key expected: " + subKey);
        }
        arguments = args;
    }
}
