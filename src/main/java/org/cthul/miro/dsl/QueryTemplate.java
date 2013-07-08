package org.cthul.miro.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.dsl.QueryBuilder.PartType;
import org.cthul.miro.dsl.QueryBuilder.QueryPart;
import org.cthul.miro.util.SqlUtils;

/**
 *
 */
public class QueryTemplate<E> {
    
    private final List<String> selectableFields = new ArrayList<>();
    private final List<String> defaultFields = new ArrayList<>();
    private final List<String> alwaysRequired = new ArrayList<>();
    private final Map<String, PartTemplate<E>> parts = new HashMap<>();

    public QueryTemplate() {
    }

    public List<String> getSelectableFields() {
        return selectableFields;
    }

    public List<String> getDefaultFields() {
        return defaultFields;
    }

    public List<String> getAlwaysRequired() {
        return alwaysRequired;
    }

    public PartTemplate<E> getPart(String key) {
        return parts.get(key);
    }
    
    protected void addPart(PartTemplate<E> pt) {
        parts.put(pt.key, pt);
        if (pt.required == ALWAYS_REQUIRED) {
            alwaysRequired.add(pt.key);
        } else if (pt.required == BY_DEFAULT) {
            defaultFields.add(pt.key);
        }
    }
    
    protected String[] copyDependencies(String[] source, int index) {
        if (source.length <= index || source[index] == null) {
            return NO_DEPENDENCIES;
        }
        return Arrays.copyOfRange(source, index, source.length);
    }
    
    protected void select(String... selectClause) {
        select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void select(String selectClause) {
        select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void select(String[] required, String... selectClause) {
        for (String s: selectClause) {
            select(required, s);
        }
    }
    
    protected void select(String[] required, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate<E> sp = new SimplePartTemplate<>(PartType.SELECT, part[0], part[1], req);
            select(sp);
        }
    }
    
    protected void select(PartTemplate<E>... selectParts) {
        for (PartTemplate<E> s: selectParts) {
            select(s);
        }
    }
    
    protected void select(PartTemplate<E> selectPart) {
        selectableFields.add(selectPart.key);
        defaultFields.add(selectPart.key);
        internal_select(selectPart);
    }
    
    protected void optional_select(String... selectClause) {
        optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void optional_select(String selectClause) {
        optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void optional_select(String[] required, String... selectClause) {
        for (String s: selectClause) {
            optional_select(required, s);
        }
    }
    
    protected void optional_select(String[] required, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }            PartTemplate<E> sp = new SimplePartTemplate<>(PartType.SELECT, part[0], part[1], req);
            optional_select(sp);
        }
    }
    
    protected void optional_select(PartTemplate<E>... selectParts) {
        for (PartTemplate<E> s: selectParts) {
            optional_select(s);
        }
    }
    
    protected void optional_select(PartTemplate<E> selectPart) {
        selectableFields.add(selectPart.key);
        internal_select(selectPart);
    }
    
    protected void internal_select(String... selectClause) {
        internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void internal_select(String selectClause) {
        internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void internal_select(String[] required, String... selectClause) {
        for (String s: selectClause) {
            internal_select(required, s);
        }
    }
    
    protected void internal_select(String[] required, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate<E> sp = new SimplePartTemplate<>(PartType.SELECT_INTERNAL, part[0], part[1], req);
            internal_select(sp);
        }
    }
    
    protected void internal_select(PartTemplate<E>... selectParts) {
        for (PartTemplate<E> s: selectParts) {
            internal_select(s);
        }
    }
    
    protected void internal_select(PartTemplate<E> selectPart) {
        addPart(selectPart);
    }
    
    protected void from(String from) {
        String[] part = SqlUtils.parseFromPart(from);
        PartTemplate<E> fp = new SimplePartTemplate<>(PartType.FROM, part[0], part[1], ALWAYS_REQUIRED);
        from(fp);
    }
    
    protected void from(PartTemplate<E> fp) {
        alwaysRequired.add(fp.key);
        parts.put(fp.key, fp);
    }
    
    protected void join(String join) {
        join(NO_DEPENDENCIES, join);
    }
    
    protected void join(String key, String join) {
        join(NO_DEPENDENCIES, key, join);
    }
    
    protected void join(String[] required, String join) {
        String[] part = SqlUtils.parseJoinPart(join);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        join(required, part[0], part[1]);
    }
    
    protected void join(String[] required, String key, String join) {
        PartTemplate<E> jp = new SimplePartTemplate<>(PartType.JOIN, key, join, required);
        join(jp);
    }
    
    protected void join(PartTemplate<E> jp) {
        addPart(jp);
    }
    
    protected void where(String where) {
        where(NO_DEPENDENCIES, where);
    }
    
    protected void where(String... definitions) {
        where(NO_DEPENDENCIES, definitions);
    }
    
    protected void where(String key, String where) {
        where(NO_DEPENDENCIES, key, where);
    }
    
    protected void where(String[] required, String where) {
        String id = "$$where" + parts.size();
        where(required, id, where);
    }
    
    protected void where(String[] required, String... definitions) {
        if (definitions.length % 2 != 0) {
            throw new IllegalArgumentException("Expected key-definition pairs");
        }
        for (int i = 0; i < definitions.length; i += 2) {
            where(required, definitions[i], definitions[i+1]);
        }
    }
    
    protected void where(String[] required, String key, String where) {
        PartTemplate<E> jp = new SimplePartTemplate<>(PartType.WHERE, key, where, required);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        where(jp);
    }
    
    protected void where(PartTemplate<E> wp) {
        addPart(wp);
    }
    
    protected void groupBy(String groupBy) {
        groupBy(NO_DEPENDENCIES, groupBy);
    }
    
    protected void groupBy(String key, String groupBy) {
        groupBy(NO_DEPENDENCIES, key, groupBy);
    }

    protected void groupBy(String[] required, String groupBy) {
        String[] part = SqlUtils.parseSelectPart(groupBy);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        PartTemplate<E> jp = new SimplePartTemplate<>(PartType.GROUP, "groupBy_"+part[0], part[1], required);
        groupBy(jp);
    }
    
    protected void groupBy(String[] required, String key, String groupBy) {
        PartTemplate<E> gp = new SimplePartTemplate<>(PartType.GROUP, key, groupBy, required);
        groupBy(gp);
    }
    
    protected void groupBy(PartTemplate<E> gp) {
        addPart(gp);
    }

    protected void having(String having) {
        having(NO_DEPENDENCIES, having);
    }
    
    protected void having(String key, String having) {
        having(NO_DEPENDENCIES, key, having);
    }
    
    protected void having(String[] required, String having) {
        String id = "$$having" + parts.size();
        having(required, id, having);
    }
    
    protected void having(String[] required, String key, String having) {
        PartTemplate<E> hp = new SimplePartTemplate<>(PartType.HAVING, key, having, required);
        having(hp);
    }
    
    protected void having(PartTemplate<E> hp) {
        addPart(hp);
    }

    protected void orderBy(String order) {
        orderBy(NO_DEPENDENCIES, order);
    }
    
    protected void orderBy(String key, String order) {
        orderBy(NO_DEPENDENCIES, key, order);
    }
    
    protected void orderBy(String[] required, String order) {
        String id = "$$order" + parts.size();        
        orderBy(required, id, order);
    }

    protected void orderBy(String[] required, String key, String order) {
        PartTemplate<E> jp = new SimplePartTemplate<>(PartType.ORDER, key, order, required);
        orderBy(jp);
    }
    
    protected void orderBy(PartTemplate<E> gp) {
        addPart(gp);
    }
    
    protected void virtualPart(String[] required, String key) {
        PartTemplate pt = new SimplePartTemplate(PartType.VIRTUAL, key, "-virtual-part-", required);
        addPart(pt);
    }
    
    protected Using<E> always(String... keys) {
        String id = "$$always" + parts.size();
        virtualPart(keys, id);
        alwaysRequired.add(id);
        return always();
    }
    
    protected Using<E> always() {
        return using(ALWAYS_REQUIRED);
    }
    
    protected Using<E> byDefault(String... keys) {
        String id = "$$default" + parts.size();
        virtualPart(keys, id);
        defaultFields.add(id);
        return byDefault();
    }
    
    protected Using<E> byDefault() {
        return using(BY_DEFAULT);
    }
    
    protected Using<E> noDepenencies() {
        return using(NO_DEPENDENCIES);
    }

    protected Using<E> using() {
        return using(NO_DEPENDENCIES);
    }

    protected Using<E> using(String... keys) {
        return new Using<>(keys);
    }

    public class Using<E> {

        private final String[] required;

        public Using(String[] required) {
            this.required = required;
        }

        public Using<E> select(String... selectClause) {
            select(required, selectClause);
            return this;
        }

        public Using<E> select(String selectClause) {
            select(required, selectClause);
            return this;
        }

        public Using<E> optional_select(String... selectClause) {
            optional_select(required, selectClause);
            return this;
        }

        public Using<E> optional_select(String selectClause) {
            optional_select(required, selectClause);
            return this;
        }

        public Using<E> internal_select(String... selectClause) {
            internal_select(required, selectClause);
            return this;
        }

        public Using<E> internal_select(String selectClause) {
            internal_select(required, selectClause);
            return this;
        }

        public Using<E> join(String join) {
            join(required, join);
            return this;
        }

        public Using<E> join(String key, String join) {
            join(required, key, join);
            return this;
        }

        public Using<E> where(String where) {
            where(required, where);
            return this;
        }

        public Using<E> where(String key, String where) {
            where(required, key, where);
            return this;
        }
        
        public Using<E> where(String... definitions) {
            where(required, definitions);
            return this;
        }

        public Using<E> groupBy(String groupBy) {
            groupBy(required, groupBy);
            return this;
        }

        public Using<E> groupBy(String key, String groupBy) {
            groupBy(required, key, groupBy);
            return this;
        }

        public Using<E> having(String having) {
            having(required, having);
            return this;
        }

        public Using<E> having(String key, String having) {
            having(required, key, having);
            return this;
        }

        public Using<E> orderBy(String order) {
            orderBy(required, order);
            return this;
        }

        public Using<E> orderBy(String key, String order) {
            orderBy(required, key, order);
            return this;
        }
    }
    
    public static abstract class PartTemplate<Entity> {
        protected final String key;
        protected final String[] required;

        public PartTemplate(String key, String[] required) {
            this.key = key;
            this.required = required;
        }

        public abstract QueryPart<Entity> createPart(String alias);
    }
    
    public static class SimplePartTemplate<Entity> extends PartTemplate<Entity> {
        private final String definition;
        final PartType type;

        public SimplePartTemplate(PartType type, String key, String definition, String[] required) {
            super(key, required);
            this.definition = definition;
            this.type = type;
        }

        @Override
        public QueryPart<Entity> createPart(String alias) {
            return new QueryBuilder.CustomPart<>(alias, type, definition);
        }
    }
    
    public static class AdapterPartTemplate<Entity> extends PartTemplate<Entity> {

        private final ValueAdapterFactory<Entity> f;
        
        public AdapterPartTemplate(String key,  ValueAdapterFactory<Entity> f, String[] required) {
            super(key, required);
            this.f = f;
        }

        @Override
        public QueryPart<Entity> createPart(String alias) {
            return new QueryBuilder.ValueAdapterPart<>(alias, f);
        }
        
    }
    
    protected static final String[] AUTODETECT_DEPENDENCIES = null;
    protected static final String[] NO_DEPENDENCIES = {};
    protected static final String[] ALWAYS_REQUIRED = {};
    protected static final String[] BY_DEFAULT = {};
    
}
