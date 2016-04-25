package org.cthul.miro.sql.map;

import java.util.List;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.request.StatementHolder;
import org.cthul.miro.request.impl.SimpleRequestComposer;
import org.cthul.miro.sql.template.SqlComposerKey;
import org.cthul.miro.sql.template.SqlTemplatesBuilder;
import org.cthul.miro.sql.template.SqlTemplates;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.TemplateLayerStack;
import org.cthul.miro.db.MiException;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.map.MappedType;
import org.cthul.miro.map.MappingHolder;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.layer.MappedQuery;
import org.cthul.miro.request.part.ListNode;
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
    
    @Override
    public SqlTemplatesBuilder<?> internalSqlTemplatesBuilder() {
        return sqlTemplates;
    }
    
    public TemplateLayer<MappedQuery<Entity, SelectQuery>> getSelectLayer() {
        return TemplateLayerStack.join(
            MappingHolder.wrapped(getMaterializationLayer()),
            StatementHolder.wrapped(sqlTemplates.getSelectLayer()));
    }

    @Override
    protected Key<ListNode<Object[]>> getColumnFilterKey(List<String> columns) {
        String[] ary = columns.toArray(new String[columns.size()]);
        return new SqlComposerKey.AttributeFilterKey(ary);
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
        String[] keyArray = getKeys().toArray(new String[0]);
        return new AbstractBatchLoader() {
            RequestComposer<MappedQuery<Entity, SelectQuery>> batch = null;
            @Override
            protected void fillAttributes(EntityType<Entity> type, List<Object[]> keys) throws MiException {
                if (batch == null) {
                    batch = batchComposer.copy();
                    batch.node(MappingKey.TYPE).setType(type);
                    batch.node(MappingKey.INCLUDE).addAll(getKeys());
                    batch.node(MappingKey.LOAD).addAll(flattenStr(attributes));
                }
                RequestComposer<MappedQuery<Entity, SelectQuery>> cmp = batch.copy();
                cmp.node(MappingKey.PROPERTY_FILTER).forProperties(keyArray).addAll(keys);
                MappedQuery<Entity, SelectQuery> query = new MappedQuery<>(graph, SqlDQML.select());
                query.query(cmp)._get().noResult();
            }
        };
    }
}
