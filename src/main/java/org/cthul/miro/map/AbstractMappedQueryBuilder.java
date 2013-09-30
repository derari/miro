package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.result.*;

/**
 * Converts a {@link QueryBuilder} into a {@link MappedStatement}
 * @param <Entity> 
 */
public abstract class AbstractMappedQueryBuilder<Entity> extends MappedStatement<Entity> {

    public AbstractMappedQueryBuilder(MiConnection cnn, Mapping<Entity> mapping) {
        super(cnn, mapping);
    }

    protected abstract QueryBuilder queryBuilder();

    @Override
    protected List<String> selectedFields() {
        return queryBuilder().getSelectedFields();
    }

    @Override
    protected String queryString() {
        return queryBuilder().getQueryString();
    }

    @Override
    protected Object[] arguments() {
        return queryBuilder().getArguments().toArray();
    }
    
    @Override
    public void put(String key) {
        queryBuilder().put(key);
    }
    
    @Override
    public void put(String key, Object... args) {
        queryBuilder().put(key, args);
    }
    
    @Override
    public void put(String key, String subKey, Object... args) {
        queryBuilder().put(key, subKey, args);
    }

    @Override
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
        super.addMoreConfigs(cnn, configs);
        for (QueryBuilder.QueryPart qp: queryBuilder().getAllParts()) {
            if (qp instanceof ConfigurationPart) {
                ConfigurationPart<? super Entity> sp = (ConfigurationPart<Entity>) qp;
                configs.add(sp.getConfiguration(cnn, mapping));
            }
        }
    }
}
