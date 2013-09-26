package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.query.SqlQueryBuilder;
import org.cthul.miro.result.EntitySetup;

/**
 *
 */
public class MappedSqlQuery<Entity> extends AbstractMappedQueryBuilder<Entity> {
    
    private final SqlQueryBuilder sql = new SqlQueryBuilder();
    private List<EntitySetup<? super Entity>> setups = null;

    public MappedSqlQuery(MiConnection cnn, Mapping<Entity> mapping) {
        super(cnn, mapping);
    }

    public SqlQueryBuilder query() {
        return sql;
    }

    @Override
    protected QueryBuilder queryBuilder() {
        return sql;
    }
    
    public void addSetup(EntitySetup<? super Entity> setup) {
        if (setups == null) setups = new ArrayList<>();
        setups.add(setup);
    }
    
    public void addSetups(EntitySetup<? super Entity>... setups) {
        if (this.setups == null) this.setups = new ArrayList<>();
        this.setups.addAll(Arrays.asList(setups));
    }

    @Override
    protected void addMoreSetups(MiConnection cnn, List<EntitySetup<? super Entity>> setups) {
        super.addMoreSetups(cnn, setups);
        if (setups != null) {
            setups.addAll(this.setups);
        }
    }
}
