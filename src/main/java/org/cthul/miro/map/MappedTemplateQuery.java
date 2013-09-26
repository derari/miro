package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.query.QueryTemplate;
import org.cthul.miro.query.QueryWithTemplate;
import org.cthul.miro.result.EntitySetup;

/**
 *
 */
public class MappedTemplateQuery<Entity> extends AbstractMappedQueryBuilder<Entity> {
    
    private final InternQueryWithTemplate query;
    private List<EntitySetupFactory<? super Entity>> setups = null;

    public MappedTemplateQuery(MiConnection cnn, MappedTemplate<Entity> template) {
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
    
    public void setup(EntitySetupFactory<? super Entity> setup) {
        if (setups == null) setups = new ArrayList<>();
        setups.add(setup);
    }
    
    public void setup(EntitySetupFactory<? super Entity>... setups) {
        if (this.setups == null) this.setups = new ArrayList<>();
        this.setups.addAll(Arrays.asList(setups));
    }

    public void setup(Object setup) {
        setup(EntitySetupInstance.asFactory(setup));
    }
    
    public void setup(Object... setups) {
        for (Object o: setups) {
            setup(o);
        }
    }
    
    @Override
    protected void addMoreSetups(MiConnection cnn, List<EntitySetup<? super Entity>> setups) {
        super.addMoreSetups(cnn, setups);
        List<String> selected = selectedFields();
        if (setups != null) {
            for (EntitySetupFactory<? super Entity> f: this.setups) {
                setups.add(f.getSetup(cnn, mapping, selected));
            }
        }
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
        public void putUnknownKey(String key, String subKey, Object[] args) {
            super.putUnknownKey(key, subKey, args);
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
