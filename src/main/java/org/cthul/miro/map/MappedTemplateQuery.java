package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.query.QueryBuilder.QueryPart;
import org.cthul.miro.query.QueryTemplate;
import org.cthul.miro.query.QueryWithTemplate;
import org.cthul.miro.result.EntityConfiguration;

/**
 *
 */
public class MappedTemplateQuery<Entity> extends AbstractMappedQueryBuilder<Entity> {
    
    private final InternQueryWithTemplate query;
    private List<ConfigurationProvider<? super Entity>> configs = null;

    public MappedTemplateQuery(MiConnection cnn, MappedQueryTemplate<Entity> template) {
        this(cnn, template.getMapping(), template);
    }
    
    public MappedTemplateQuery(MiConnection cnn, Mapping<Entity> mapping, QueryTemplate template) {
        super(cnn, mapping);
        this.query = new InternQueryWithTemplate(template);
    }

    protected InternQueryWithTemplate query() {
        return query;
    }

    @Override
    protected InternQueryWithTemplate queryBuilder() {
        return query;
    }
    
    public void configure(ConfigurationProvider<? super Entity> config) {
        if (configs == null) configs = new ArrayList<>();
        configs.add(config);
    }
    
    public void configure(Object setup) {
        configure(ConfigurationInstance.asFactory(setup));
    }
    
    public void configure(Object... setups) {
        for (Object o: setups) {
            configure(o);
        }
    }
    
    @Override
    protected void addMoreConfigs(MiConnection cnn, List<EntityConfiguration<? super Entity>> configs) {
        super.addMoreConfigs(cnn, configs);
        if (this.configs != null) {
            for (ConfigurationProvider<? super Entity> f: this.configs) {
                configs.add(f.getConfiguration(cnn, mapping));
            }
        }
    }
    
    protected void selectAll() {
        query().selectAll();
    }

    protected void selectAllOptional() {
        query().selectAllOptional();
    }

    protected void select(String... keys) {
        query().select(keys);
    }

    protected void where(String key, Object... args) {
        query().where(key, args);
    }

    protected void groupBy(String key) {
        query().groupBy(key);
    }

    protected void groupBy(String... keys) {
        query().groupBy(keys);
    }

    protected void having(String key, Object... args) {
        query().having(key, args);
    }

    protected void orderBy(String key) {
        query().orderBy(key);
    }

    protected void orderBy(String... key) {
        query().orderBy(key);
    }

    protected QueryPart addPart(String key) {
        return query().addPart(key);
    }

    protected QueryPart addPartAs(String key, String alias) {
        return query().addPartAs(key, alias);
    }

    protected void sql_select(String... selectClause) {
        query().sql_select(selectClause);
    }

    protected void sql_select(String selectClause) {
        query().sql_select(selectClause);
    }

    protected QueryPart sql_from(String from) {
        return query().sql_from(from);
    }

    protected QueryPart sql_join(String join) {
        return query().sql_join(join);
    }

    protected QueryPart sql_where(String where) {
        return query().sql_where(where);
    }

    protected QueryPart sql_groupBy(String groupBy) {
        return query().sql_groupBy(groupBy);
    }

    protected QueryPart sql_having(String having) {
        return query().sql_having(having);
    }

    protected QueryPart sql_orderBy(String order) {
        return query().sql_orderBy(order);
    }

    protected void select(QueryPart... selectParts) {
        query().select(selectParts);
    }

    protected QueryPart select(QueryPart sp) {
        return query().select(sp);
    }

    protected QueryPart internalSelect(QueryPart sp) {
        return query().internalSelect(sp);
    }

    protected QueryPart from(QueryPart fp) {
        return query().from(fp);
    }

    protected QueryPart join(QueryPart jp) {
        return query().join(jp);
    }

    protected QueryPart where(QueryPart wp) {
        return query().where(wp);
    }

    protected QueryPart groupBy(QueryPart gp) {
        return query().groupBy(gp);
    }

    protected QueryPart having(QueryPart hp) {
        return query().having(hp);
    }

    protected QueryPart orderBy(QueryPart op) {
        return query().orderBy(op);
    }

    protected QueryPart other(QueryPart ap) {
        return query().other(ap);
    }

    protected QueryPart addPart(QueryPart qp) throws IllegalArgumentException {
        return query().addPart(qp);
    }
    
    protected static class InternQueryWithTemplate extends QueryWithTemplate {

        public InternQueryWithTemplate(QueryTemplate template) {
            super(template);
        }

        @Override
        public void selectAll() {
            super.selectAll();
        }

        @Override
        public void selectAllOptional() {
            super.selectAllOptional();
        }

        @Override
        public void select(String... keys) {
            super.select(keys);
        }

        @Override
        public void where(String key, Object... args) {
            super.where(key, args);
        }

        @Override
        public void groupBy(String key) {
            super.groupBy(key);
        }

        @Override
        public void groupBy(String... keys) {
            super.groupBy(keys);
        }

        @Override
        public void having(String key, Object... args) {
            super.having(key, args);
        }

        @Override
        public void orderBy(String key) {
            super.orderBy(key);
        }

        @Override
        public void orderBy(String... key) {
            super.orderBy(key);
        }

        @Override
        public QueryPart addPart(String key) {
            return super.addPart(key);
        }

        @Override
        public QueryPart addPartAs(String key, String alias) {
            return super.addPartAs(key, alias);
        }

        @Override
        public void sql_select(String... selectClause) {
            super.sql_select(selectClause);
        }

        @Override
        public void sql_select(String selectClause) {
            super.sql_select(selectClause);
        }

        @Override
        public QueryPart sql_from(String from) {
            return super.sql_from(from);
        }

        @Override
        public QueryPart sql_join(String join) {
            return super.sql_join(join);
        }

        @Override
        public QueryPart sql_where(String where) {
            return super.sql_where(where);
        }

        @Override
        public QueryPart sql_groupBy(String groupBy) {
            return super.sql_groupBy(groupBy);
        }

        @Override
        public QueryPart sql_having(String having) {
            return super.sql_having(having);
        }

        @Override
        public QueryPart sql_orderBy(String order) {
            return super.sql_orderBy(order);
        }

        @Override
        public void select(QueryPart... selectParts) {
            super.select(selectParts);
        }

        @Override
        public QueryPart select(QueryPart sp) {
            return super.select(sp);
        }

        @Override
        public QueryPart internalSelect(QueryPart sp) {
            return super.internalSelect(sp);
        }

        @Override
        public QueryPart from(QueryPart fp) {
            return super.from(fp);
        }

        @Override
        public QueryPart join(QueryPart jp) {
            return super.join(jp);
        }

        @Override
        public QueryPart where(QueryPart wp) {
            return super.where(wp);
        }

        @Override
        public QueryPart groupBy(QueryPart gp) {
            return super.groupBy(gp);
        }

        @Override
        public QueryPart having(QueryPart hp) {
            return super.having(hp);
        }

        @Override
        public QueryPart orderBy(QueryPart op) {
            return super.orderBy(op);
        }

        @Override
        public QueryPart other(QueryPart ap) {
            return super.other(ap);
        }

        @Override
        public QueryPart addPart(QueryPart qp) throws IllegalArgumentException {
            return super.addPart(qp);
        }
    }
}
