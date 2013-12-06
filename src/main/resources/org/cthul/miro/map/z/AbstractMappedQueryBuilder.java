package org.cthul.miro.map.z;

import org.cthul.miro.map.ConfigurationProvider;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.query.ZQueryBuilder;
import org.cthul.miro.result.*;

/**
 * Converts a {@link ZQueryBuilder} into a {@link MappedStatement}
 * @param <Entity> 
 */
public abstract class AbstractMappedQueryBuilder<Entity> extends MappedStatement<Entity> {

    public AbstractMappedQueryBuilder(MiConnection cnn, SimpleMapping<Entity> mapping) {
        super(cnn, mapping);
    }

    protected abstract ZQueryBuilder queryBuilder();

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
    
    public void require(String... keys) {
        for (String k: keys) {
            put(k);
        }
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
    public void put2(String key, String subKey, Object... args) {
        queryBuilder().put2(key, subKey, args);
    }

    @Override
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
        super.addMoreConfigs(cnn, configs);
        for (ZQueryBuilder.QueryPart qp: queryBuilder().getAllParts()) {
            if (qp instanceof ConfigurationProvider) {
                ConfigurationProvider<? super Entity> sp = (ConfigurationProvider<Entity>) qp;
                configs.add(sp.getConfiguration(cnn, mapping));
            }
        }
    }
}
