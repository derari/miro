package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.result.*;

public class OrderedResult<Entity> implements ResultBuilder<List<Entity>, Entity> {
    
    private final Mapping<Entity> mapping;
    private final String[] fields;
    private final List<Object[]> values = new ArrayList<>();

    public OrderedResult(Mapping<Entity> mapping, String... fields) {
        this.mapping = mapping;
        this.fields = fields;
    }
    
    public void expectEntity(Object... values) {
        this.values.add(values);
    }
    
    @Override
    public List<Entity> build(ResultSet rs, EntityType<Entity> type, EntityConfiguration<? super Entity> config) throws SQLException {
        final List<Entity> entities = ResultBuilders.<Entity>getListResult().build(rs, type, config);
        KeyMap<?,Entity> map;
        if (fields.length != 1) {
            map = new KeyMap.MultiKey<>();
        } else {
            map = new KeyMap.SingleKey<>();
        }
        return orderEntities(map, entities);
    }

    private void readFields(Entity e, final Object[] tmpValues) {
        for (int i = 0; i < tmpValues.length; i++) {
            tmpValues[i] = mapping.getField(e, fields[i]);
        }
    }

    private <IK> List<Entity> orderEntities(final KeyMap<IK, Entity> map, final List<Entity> entities) {
        IK tmpKey = map.prepareInternKey();
        final Object[] tmpValues = new Object[fields.length];
        for (Entity e: entities) {
            readFields(e, tmpValues);
            tmpKey = map.internKey(tmpKey, tmpValues);
            map.putIntern(tmpKey, e);
        }
        final List<Entity> result = new ArrayList<>();
        for (Object[] expected: values) {
            tmpKey = map.internKey(tmpKey, expected);
            result.add(map.getIntern(tmpKey));
        }
        return result;
    }
}
