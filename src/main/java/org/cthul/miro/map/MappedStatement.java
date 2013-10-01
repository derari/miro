package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.result.*;
import org.cthul.miro.result.ResultBuilder;

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
    
    protected abstract List<String> selectedFields();

    protected abstract String queryString();

    protected abstract Object[] arguments();
    
    public abstract void put(String key);
    
    public abstract void put(String key, Object... args);
    
    public abstract void put(String key, String subKey, Object... args);

    public EntityConfiguration<? super Entity> getConfiguration(MiConnection cnn) {
        final List<EntityConfiguration<? super Entity>> configs = new ArrayList<>();
        configs.add(getMappingConfig(cnn));
        addMoreConfigs(cnn, configs);
        return CombinedEntityConfig.combine(configs);
    }
    
    protected EntityConfiguration<? super Entity> getMappingConfig(MiConnection cnn) {
        List<String> selected = selectedFields();
        if (selected == null) {
            return new SelectAll();
        } else {
            return mapping.newSetup(selectedFields());
        }
    }
    
    protected EntityType<Entity> getEntityType() {
        return mapping;
    }
    
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
    }

    public ResultSet runQuery() throws SQLException {
        return runQuery(cnn);
    }

    public ResultSet runQuery(MiConnection cnn) throws SQLException {
        if (cnn == null) {
            throw new IllegalArgumentException("No connection given");
        }
        MiPreparedStatement ps = cnn.prepare(queryString());
        return ps.executeQuery(arguments());
    }

    public MiFuture<ResultSet> submitQuery() throws SQLException {
        return submitQuery(cnn);
    }

    public MiFuture<ResultSet> submitQuery(MiConnection cnn) throws SQLException {
        if (cnn == null) {
            throw new IllegalArgumentException("No connection given");
        }
        MiPreparedStatement ps = cnn.prepare(queryString());
        return ps.submitQuery(arguments());
    }

    public <R> SubmittableQuery<R> as(ResultBuilder<R, Entity> rb) {
        return new SubmittableQuery<>(cnn, this, rb, getEntityType());
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
    
    protected class SelectAll implements EntityConfiguration<Entity> {

        @Override
        public EntityInitializer<Entity> newInitializer(ResultSet rs) throws SQLException {
            ResultSetMetaData meta = rs.getMetaData();
            int c = meta.getColumnCount();
            String[] columns = new String[c];
            for (int i = 0; i < c; i++) {
                columns[i] = meta.getColumnLabel(i);
            }
            return mapping.newSetup(columns).newInitializer(rs);
        }
    }
}
