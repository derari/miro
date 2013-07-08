package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.map.ResultBuilder.ValueAdapter;

/**
 * A statement with an entity mapping.
 * @param <Entity> 
 */
public abstract class MappedStatement<Entity> {

    private final MiConnection cnn;
    protected final Mapping<Entity> mapping;

    public MappedStatement(MiConnection cnn, Mapping<Entity> mapping) {
        this.cnn = cnn;
        this.mapping = mapping;
    }

    protected abstract String[] selectedFields();

    protected abstract String queryString();

    protected abstract Object[] arguments();
    
    public void put(String key) {
        put(key, (Object[]) null);
    }
    
    public void put(String key, Object... args) {
        String subKey;
        int dot = key.indexOf('.');
        if (dot < 0) {
            subKey = null;
        } else {
            subKey = key.substring(dot+1);
            key = key.substring(0, dot);
        }
        put(key, subKey, args);
    }
    
    public abstract void put(String key, String subKey, Object... args);

    /* SubmittableQuery */
    protected ValueAdapter<? super Entity> buildValueAdapter(MiConnection cnn) {
        ValueAdapter<Entity> va = mapping.newValueAdapter(selectedFields());
        return moreValueAdapters(cnn, va);
    }

    protected ValueAdapter<? super Entity> moreValueAdapters(MiConnection cnn, ValueAdapter<? super Entity> entityAdapter) {
        return entityAdapter;
    }

    public ResultSet runQuery() throws SQLException {
        return runQuery(cnn);
    }

    public ResultSet runQuery(MiConnection cnn) throws SQLException {
        MiPreparedStatement ps = cnn.prepare(queryString());
        return ps.executeQuery(arguments());
    }

    public MiFuture<ResultSet> submitQuery() throws SQLException {
        return submitQuery(cnn);
    }

    public MiFuture<ResultSet> submitQuery(MiConnection cnn) throws SQLException {
        MiPreparedStatement ps = cnn.prepare(queryString());
        return ps.submitQuery(arguments());
    }

    public <R> SubmittableQuery<R> as(ResultBuilder<R, Entity> rb) {
        return new SubmittableQuery<>(cnn, this, rb, mapping.newEntityFactory());
    }

    public SubmittableQuery<Entity[]> asArray() {
        return as(mapping.asArray());
    }

    public SubmittableQuery<List<Entity>> asList() {
        return as(mapping.asList());
    }

    public SubmittableQuery<ResultCursor<Entity>> asCursor() {
        return as(mapping.asCursor());
    }

    public SubmittableQuery<Entity> getSingle() {
        return as(mapping.getSingle());
    }

    public SubmittableQuery<Entity> getFirst() {
        return as(mapping.getFirst());
    }
}
