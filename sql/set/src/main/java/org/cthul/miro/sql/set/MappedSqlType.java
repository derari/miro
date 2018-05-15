package org.cthul.miro.sql.set;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.request.impl.SimpleRequestComposer;
import org.cthul.miro.sql.template.SqlComposerKey;
import org.cthul.miro.sql.template.SqlTemplatesBuilder;
import org.cthul.miro.sql.template.SqlTemplates;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.sql.template.AttributeFilter;
import org.cthul.miro.sql.template.JoinedView;
import org.cthul.miro.sql.template.SqlAttribute;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Entity>
 */
public class MappedSqlType<Entity>
        extends MappedType<Entity, MappedSqlType<Entity>>
        implements MappedSqlBuilder<Entity,MappedSqlType<Entity>>, 
        SqlTemplatesBuilder.Delegator<MappedSqlType<Entity>> {

    private final SqlTemplates sqlTemplates;
    private RequestComposer<MappedQuery<Entity, SelectQuery>> batchComposer = null;

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
    
    public TemplateLayer<MappedQuery<Entity, SelectQuery>> getSelectLayer() {
        return null;
//        return TemplateLayerStack.join(
//            MappingHolder.wrapped(getMaterializationLayer()),
//            StatementHolder.wrapped(sqlTemplates.getSelectLayer()));
    }
    
//    public <Builder> MappedSelectNodeFactory<Entity, MappedQuery<Entity,SelectQuery>> getMappedSelectNodeFactory() {
//        return getMappedSelectNodeFactory(Function.identity());
//    }
//    
//    public <Builder> MappedSelectNodeFactory<Entity, Builder> getMappedSelectNodeFactory(Function<? super Builder, ? extends MappedQuery<Entity,SelectQuery>> builderAdapter) {
//        return new MappedSelectNodeFactory<>(
//                this.<Builder>getMappedQueryNodeFactory(builderAdapter.andThen(q -> q.getMapping())),
//                sqlTemplates.<Builder>getSelectNodeFactory(builderAdapter.andThen(q -> q.getStatement())));
//    }

    public MappedSelectRequest<Entity> newMappedSelectComposer() {
        return MappedSelectNodeFactory.newComposer(newMappedQueryComposer(), sqlTemplates.newSelectComposer());
//        return new MappedSelectNodeFactory(newMappedQueryComposer(), sqlTemplates.newSelectComposer()).newComposer();
    }
    
    @Override
    protected Key<ListNode<Object[]>> getColumnFilterKey(List<String> columns) {
        String[] ary = columns.toArray(new String[columns.size()]);
        return AttributeFilter.key(ary);
    }

    @Override
    public Key<ListNode<String>> getResultColumnsKey() {
        return SqlComposerKey.ATTRIBUTES;
    }

    @Override
    protected BatchLoader<Entity> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
        if (batchComposer == null) {
            batchComposer = new SimpleRequestComposer<>(getSelectLayer());
        }
        // TODO: most of this can be moved up to MappedType
        String[] keyArray = getKeys().toArray(new String[0]);
        return new AbstractBatchLoader(graph) {
            RequestComposer<MappedQuery<Entity, SelectQuery>> batch = null;
            @Override
            protected void fillAttributes(EntityType<Entity> type, List<Object[]> keys) throws MiException {
                if (batch == null) {
                    batch = batchComposer.copy();
                    batch.node(MappingKey.TYPE).setGraph(graph);
                    batch.node(MappingKey.TYPE).setType(type);
                    batch.node(MappingKey.INCLUDE).addAll(getKeys());
                    batch.node(MappingKey.FETCH).addAll(flattenStr(attributes));
                }
                RequestComposer<MappedQuery<Entity, SelectQuery>> cmp = batch.copy();
                cmp.node(MappingKey.PROPERTY_FILTER).forProperties(keyArray).addAll(keys);
                MappedQuery<Entity, SelectQuery> query = new MappedQuery<>(graph, SqlDQML.select());
                query.query(cmp)._get().noResult();
            }
        };
    }
}
