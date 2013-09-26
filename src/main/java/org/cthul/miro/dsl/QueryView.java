package org.cthul.miro.dsl;

import java.util.Arrays;
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

    public QueryView(Mapping<Entity> mapping, String query, Object... args) {
        this.mapping = mapping;
        this.query = query;
        this.args = args;
    }
    
    @Override
    public MappedQueryString<Entity> select(MiConnection cnn, String... fields) {
        return new MappedQueryString<>(cnn, mapping, Arrays.asList(fields), query, args);
    }   
}
