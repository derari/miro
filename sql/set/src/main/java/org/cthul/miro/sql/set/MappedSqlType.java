package org.cthul.miro.sql.set;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.sql.composer.SqlTemplatesBuilder;
import org.cthul.miro.sql.composer.model.SqlTemplates;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.sql.composer.model.JoinedView;
import org.cthul.miro.sql.composer.model.SqlAttribute;

/**
 *
 * @param <Entity>
 */
public class MappedSqlType<Entity>
        extends MappedType<Entity, MappedSqlType<Entity>>
        implements MappedSqlBuilder<Entity,MappedSqlType<Entity>>, 
        SqlTemplatesBuilder.Delegator<MappedSqlType<Entity>> {

    private final SqlTemplates sqlTemplates;
    private MappedSelectRequest<Entity> selectComposer = null;
    private MappedSelectRequest<Entity> batchComposer = null;

    public MappedSqlType(Class<Entity> clazz) {
        super(clazz);
        sqlTemplates = new SqlTemplates(clazz.getSimpleName());
    }

    public MappedSqlType(Class<Entity> clazz, Object shortString) {
        super(clazz, shortString);
        sqlTemplates = new SqlTemplates(String.valueOf(shortString));
    }
    
    public MappedSqlType<Entity> join(String tableAlias, String prefix, MappedSqlType<?> other) {
        JoinedView jv = other.sqlTemplates.joinedAs(prefix, (keys) -> getForeignKeyExpressions(tableAlias, prefix, keys));
        join(jv);
        return this;
    }
    
    private List<Object> getForeignKeyExpressions(String tableAlias, String prefix, List<SqlAttribute> targetKeys) {
        List<Object> result = new ArrayList<>();
        targetKeys.forEach(k -> {
            String atKey = prefix + "_" + k.getKey();
            SqlAttribute at = sqlTemplates.getAttributes().get(atKey);
            if (at == null) {
                QlCode e = QlCode.ql(tableAlias).ql(".").ql(atKey);
                at = new SqlAttribute(atKey, e, null);
                at.getDependencies().add(tableAlias);
                attribute(at);
            }
            result.add(at);
        });
        return result;
    }
    
//    private List<Object> getForeignKeyExpressions(List<String> foreignKeys) {
//        List<Object> result = new ArrayList<>();
//        for (int i = 0; i < foreignKeys.size(); i++) {
//            String key = foreignKeys.get(i);
//            SqlAttribute at = sqlTemplates.getAttributes().get(key);
//            if (at != null) result.add(at.expression());
//            else result.add(MiSqlParser.parseCode(key));
//        }
//        return result;
//    }
    
//    @Override
//    public MappedSqlType<Entity> attribute(SqlAttribute attribute) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    public SqlTemplates getSqlTemplates() {
        return sqlTemplates;
    }

    @Override
    public SqlTemplatesBuilder<?> internalSqlTemplatesBuilder() {
        return sqlTemplates;
    }
    
    public MappedSelectRequest<Entity> newMappedSelectComposer() {
        if (selectComposer == null) {
            selectComposer =  MappedSelectRequestImpl.newComposer(newMappedQueryComposer(), sqlTemplates.newSelectComposer());
        }
        return selectComposer.copy();
    }

    @Override
    protected BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
        if (batchComposer == null) {
            batchComposer = newMappedSelectComposer();
        }
        // TODO: most of this can be moved up to MappedType
        String[] keyArray = getKeys().toArray(new String[0]);
        return new AbstractBatchLoader(graph) {
            MappedSelectRequest<Entity> batch = null;
            @Override
            protected void fillAttributes(EntityType<Entity> type, List<Object[]> keys) throws MiException {
                if (batch == null) {
                    batch = batchComposer.copy();
                    batch.getType().setGraph(graph);
                    batch.getType().setType(type);
                    batch.getIncludedProperties().addAll(getKeys());
                    batch.getFetchedProperties().addAll(flattenStr(attributes));
                }
                MappedSelectRequest<Entity> cmp = batch.copy();
                cmp.getPropertyFilter().forProperties(keyArray).addAll(keys);
                MappedQuery<Entity, SelectQuery> query = new MappedQuery<>(graph, SqlDQML.select());
                query.query(cmp)._get().noResult();
            }
        };
    }
}
