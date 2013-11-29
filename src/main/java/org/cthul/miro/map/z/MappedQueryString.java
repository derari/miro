package org.cthul.miro.map.z;

import org.cthul.miro.map.ConfigurationInstance;
import java.util.*;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public class MappedQueryString<Entity> extends AbstractMappedStatement<Entity> {
    
    private final List<String> fields;
    private final String query;
    private Object[] arguments;
    private List<Object> configs = null;

    public MappedQueryString(MiConnection cnn, SimpleMapping<Entity> mapping, List<String> fields, String query, Object... arguments) {
        super(cnn, mapping);
        this.fields = fields;
        this.query = query;
        this.arguments = arguments;
    }
    
    public MappedQueryString(MiConnection cnn, SimpleMapping<Entity> mapping, List<String> fields, String query) {
        super(cnn, mapping);
        this.fields = fields;
        this.query = query;
        this.arguments = null;
    }
    
    public MappedQueryString(MiConnection cnn, SimpleMapping<Entity> mapping, String query, String... fields) {
        super(cnn, mapping);
        this.fields = Arrays.asList(fields);
        this.query = query;
        this.arguments = null;
    }
    
    public MappedQueryString<Entity> configure(Object... configs) {
        return configure(Arrays.asList(configs));
    }
    
    public MappedQueryString<Entity> configure(Collection<?> configs) {
        if (this.configs == null) {
            this.configs = new ArrayList<>();
        }
        this.configs.addAll(configs);
        return this;
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
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
        super.addMoreConfigs(cnn, configs);
        if (this.configs != null) {
            for (Object o: this.configs) {
                configs.add(ConfigurationInstance.asConfiguration(o, cnn, mapping));
            }
        }
    }

    @Override
    public void put2(String key, String subKey, Object... args) {
        if (key != null && !key.isEmpty()) {
            throw new IllegalArgumentException("No key expected: " + key);
        }
        if (subKey != null && !subKey.isEmpty()) {
            throw new IllegalArgumentException("No sub-key expected: " + subKey);
        }
        arguments = args;
    }
}
