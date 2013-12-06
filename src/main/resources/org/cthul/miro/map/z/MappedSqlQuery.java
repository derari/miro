package org.cthul.miro.map.z;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.query.ZQueryBuilder;
import org.cthul.miro.query.ZSqlQueryBuilder;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public class MappedSqlQuery<Entity> extends AbstractMappedQueryBuilder<Entity> {
    
    private final ZSqlQueryBuilder sql = new ZSqlQueryBuilder();
    private List<EntityConfiguration<? super Entity>> configs = null;

    public MappedSqlQuery(MiConnection cnn, SimpleMapping<Entity> mapping) {
        super(cnn, mapping);
    }

    public ZSqlQueryBuilder query() {
        return sql;
    }

    @Override
    protected ZQueryBuilder queryBuilder() {
        return sql;
    }
    
    public void configure(EntityConfiguration<? super Entity> config) {
        if (configs == null) configs = new ArrayList<>();
        configs.add(config);
    }
    
    public void configure(EntityConfiguration<? super Entity>... configs) {
        if (this.configs == null) this.configs = new ArrayList<>();
        this.configs.addAll(Arrays.asList(configs));
    }

    @Override
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
        super.addMoreConfigs(cnn, configs);
        if (configs != null) {
            configs.addAll(this.configs);
        }
    }
}