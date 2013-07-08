package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.dsl.View;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.map.ValueAdapterBase;

/**
 *
 */
public class RelationAdatper<Entity> extends ValueAdapterBase<Entity> {

    private final Graph graph;
    private final View<? extends SelectByKey<?>> view;
    private final Mapping<Entity> mapping;
    private final String field;
    private final String[] refFields;
    private final boolean compositeKey;
    
    private ResultSet rs = null;
    private int[] refFieldIndices = null;
    private final List<Object> refKeys = new ArrayList<>();
    private final List<Entity> values = new ArrayList<>();

    public RelationAdatper(Graph graph, View<? extends SelectByKey<?>> view, Mapping<Entity> mapping, String field, String[] refFields) {
        this.graph = graph;
        this.view = view;
        this.mapping = mapping;
        this.field = field;
        this.refFields = refFields;
        this.compositeKey = refFields.length > 1;
    }
    
    @Override
    public void initialize(ResultSet rs) throws SQLException {
        this.rs = rs;
        refFieldIndices = getFieldIndices(rs, refFields);
    }

    @Override
    public void apply(Entity entity) throws SQLException {
        final Object refKey;
        if (compositeKey) {
            final Object[] keys = new Object[refFieldIndices.length];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = rs.getObject(refFieldIndices[i]);
            }
            refKey = keys;
        } else {
            refKey = rs.getObject(refFieldIndices[0]);
        }
        refKeys.add(refKey);
        values.add(entity);
    }

    @Override
    public void complete() throws SQLException {
        List<Object> objects = graph.getObjects(view, refKeys);
        final int len = values.size();
        for (int i = 0; i < len; i++) {
            mapping.setField(values.get(i), field, objects.get(i));
        }
        refKeys.clear();
        values.clear();
    }

    @Override
    public void close() throws SQLException {
    }
    
}
