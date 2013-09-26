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
        return queryBuilder().getAllParts().toArray();
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
    protected void addMoreSetups(MiConnection cnn, List<EntitySetup<? super Entity>> setups) {
        super.addMoreSetups(cnn, setups);
        for (QueryBuilder.QueryPart qp: queryBuilder().getAllParts()) {
            if (qp instanceof SetupProvider) {
                SetupProvider<? super Entity> sp = (SetupProvider<Entity>) qp;
                setups.add(sp.getSetup(cnn, mapping));
            }
        }
    }
}
