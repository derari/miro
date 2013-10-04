package org.cthul.miro.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.MappedQueryString;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class QueryView<Entity> extends ViewBase<MappedQueryString<Entity>> {

    private final Mapping<Entity> mapping;
    private final String query;
    private final Object[] args;
    private List<Object> configs = null;

    public QueryView(Mapping<Entity> mapping, String query, Object... args) {
        this.mapping = mapping;
        this.query = query;
        this.args = args;
    }
    
    @Override
    public MappedQueryString<Entity> select(MiConnection cnn, String... fields) {
        MappedQueryString qry = new MappedQueryString(cnn, mapping, fields == null ? null : Arrays.asList(fields), query, args);
        if (configs != null) {
            qry.configure(configs);
        }
        return qry;
    }
    
    public QueryView<Entity> configure(Object... configs) {
        if (this.configs == null) {
            this.configs = new ArrayList<>();
        }
        this.configs.addAll(Arrays.asList(configs));
        return this;
    }
}
