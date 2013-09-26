package org.cthul.miro.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cthul.miro.query.QueryBuilder.PartType;
import org.cthul.miro.query.QueryBuilder.QueryPart;
import org.cthul.miro.util.SqlUtils;

/**
 *
 */
public class QueryTemplate {
    
    private final List<String> selectableFields = new ArrayList<>();
    private final List<String> defaultFields = new ArrayList<>();
    private final List<String> alwaysRequired = new ArrayList<>();
    private final Map<String, PartTemplate> parts = new HashMap<>();

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

    public PartTemplate getPart(String key) {
        return parts.get(key);
    }
    
    protected void addPart(PartTemplate pt) {
        parts.put(pt.key, pt);
        if (pt.include == Include.ALWAYS) {
            alwaysRequired.add(pt.key);
        } else if (pt.include == Include.DEFAULT) {
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
            select(required, Include.DEFAULT, s);
        }
    }
    
    protected void select(String[] required, Include include, String... selectClause) {
        for (String s: selectClause) {
            select(required, include, s);
        }
    }
    
    protected void select(String[] required, Include include, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate sp = new SimplePartTemplate(PartType.SELECT, part[0], include, part[1], req);
            select(sp);
        }
    }
    
    protected void select(PartTemplate... selectParts) {
        for (PartTemplate s: selectParts) {
            select(s);
        }
    }
    
    protected void select(PartTemplate selectPart) {
        selectableFields.add(selectPart.key);
        addPart(selectPart);
    }
    
    protected void optional_select(String... selectClause) {
        optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void optional_select(String selectClause) {
        optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void optional_select(String[] required, String... selectClause) {
        for (String s: selectClause) {
            select(required, Include.OPTIONAL, s);
        }
    }
    
    protected void internal_select(String... selectClause) {
        internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void internal_select(String selectClause) {
        internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected void internal_select(String[] required, String... selectClause) {
        for (String s: selectClause) {
            internal_select(required, Include.OPTIONAL, s);
        }
    }
    
    protected void internal_select(String[] required, Include include, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate sp = new SimplePartTemplate(PartType.SELECT_INTERNAL, part[0], include, part[1], req);
            internal_select(sp);
        }
    }
    
    protected void internal_select(PartTemplate... selectParts) {
        for (PartTemplate s: selectParts) {
            internal_select(s);
        }
    }
    
    protected void internal_select(PartTemplate selectPart) {
        addPart(selectPart);
    }
    
    protected void from(String from) {
        String[] part = SqlUtils.parseFromPart(from);
        PartTemplate fp = new SimplePartTemplate(PartType.FROM, part[0], Include.ALWAYS, part[1], NO_DEPENDENCIES);
        from(fp);
    }
    
    protected void from(PartTemplate fp) {
        addPart(fp);
    }
    
    protected void join(String join) {
        join(NO_DEPENDENCIES, join);
    }
    
    protected void join(String key, String join) {
        join(NO_DEPENDENCIES, key, join);
    }
    
    protected void join(String[] required, String join) {
        join(required, Include.OPTIONAL, join);
   }

    protected void join(String[] required, Include include, String join) {
        String[] part = SqlUtils.parseJoinPart(join);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        join(required, part[0], include, part[1]);
    }
    
    protected void join(String[] required, String key, String join) {
        join(required, key, Include.OPTIONAL, join);
    }
    
    protected void join(String[] required, String key, Include include, String join) {
        PartTemplate jp = new SimplePartTemplate(PartType.JOIN, key, include, join, required);
        join(jp);
    }
    
    protected void join(PartTemplate jp) {
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
        where(required, Include.OPTIONAL, where);
   }

    protected void where(String[] required, Include include, String where) {
        String id = "$$where" + parts.size();
        where(required, id, include, where);
    }
    
    protected void where(String[] required, String... definitions) {
        where(required, Include.OPTIONAL, definitions);
    }
    
    protected void where(String[] required, Include include, String... definitions) {
        if (definitions.length % 2 != 0) {
            throw new IllegalArgumentException("Expected key-definition pairs");
        }
        for (int i = 0; i < definitions.length; i += 2) {
            where(required, definitions[i], include, definitions[i+1]);
        }
    }
    
    protected void where(String[] required, String key, String where) {
        where(required, key, Include.OPTIONAL, where);
    }
    
    protected void where(String[] required, String key, Include include, String where) {
        PartTemplate jp = new SimplePartTemplate(PartType.WHERE, key, include, where, required);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        where(jp);
    }
    
    protected void where(PartTemplate wp) {
        addPart(wp);
    }
    
    protected void groupBy(String groupBy) {
        groupBy(AUTODETECT_DEPENDENCIES, groupBy);
    }
    
    protected void groupBy(String key, String groupBy) {
        groupBy(AUTODETECT_DEPENDENCIES, key, groupBy);
    }

    protected void groupBy(String[] required, String groupBy) {
        groupBy(required, Include.OPTIONAL, groupBy);
   }

    protected void groupBy(String[] required, Include include, String groupBy) {
        String[] part = SqlUtils.parseSelectPart(groupBy);
        if (required == AUTODETECT_DEPENDENCIES) {
            required = copyDependencies(part, 2);
        }
        groupBy(required, "groupBy_"+part[0], include, part[1]);
    }
    
    protected void groupBy(String[] required, String key, String groupBy) {
        groupBy(required, key, Include.OPTIONAL, groupBy);
    }
    
    protected void groupBy(String[] required, String key, Include include, String groupBy) {
        PartTemplate gp = new SimplePartTemplate(PartType.GROUP, key, include, groupBy, required);
        groupBy(gp);
    }
    
    protected void groupBy(PartTemplate gp) {
        addPart(gp);
    }

    protected void having(String having) {
        having(NO_DEPENDENCIES, having);
    }
    
    protected void having(String key, String having) {
        having(NO_DEPENDENCIES, key, having);
    }
    
    protected void having(String[] required, String having) {
        having(required, Include.OPTIONAL, having);
   }

    protected void having(String[] required, Include include, String having) {
        String id = "$$having" + parts.size();
        having(required, id, include, having);
    }
    
    protected void having(String[] required, String key, String having) {
        having(required, key, Include.OPTIONAL, having);
    }
    
    protected void having(String[] required, String key, Include include, String having) {
        PartTemplate hp = new SimplePartTemplate(PartType.HAVING, key, include, having, required);
        having(hp);
    }
    
    protected void having(PartTemplate hp) {
        addPart(hp);
    }

    protected void orderBy(String order) {
        orderBy(NO_DEPENDENCIES, order);
    }
    
    protected void orderBy(String key, String order) {
        orderBy(NO_DEPENDENCIES, key, order);
    }
    
    protected void orderBy(String[] required, String order) {
        orderBy(required, Include.OPTIONAL, order);
   }

    protected void orderBy(String[] required, Include include, String order) {
        String id = "$$order" + parts.size();        
        orderBy(required, id, include, order);
    }

    protected void orderBy(String[] required, String key, String order) {
        orderBy(required, key, Include.OPTIONAL, order);
    }
    
    protected void orderBy(String[] required, String key, Include include, String order) {
        PartTemplate jp = new SimplePartTemplate(PartType.ORDER, key, include, order, required);
        orderBy(jp);
    }
    
    protected void orderBy(PartTemplate gp) {
        addPart(gp);
    }
    
    protected void virtualPart(String[] required, String key) {
        virtualPart(required, Include.OPTIONAL, key);
    }
    
    protected void virtualPart(String[] required, Include include, String key) {
        PartTemplate pt = new SimplePartTemplate(PartType.VIRTUAL, key, include, "-virtual-part-", required);
        addPart(pt);
    }
    
    protected void virtualPart(String[] required, Include include) {
        virtualPart(required, include, "$$virtual" + parts.size());
    }
    
    protected Using always() {
        return using(Include.ALWAYS);
    }
    
    protected Using always(String... keys) {
        virtualPart(keys, Include.ALWAYS);
        return using(Include.ALWAYS, keys);
    }
    
    protected Using byDefault() {
        return using(Include.DEFAULT, NO_DEPENDENCIES);
    }
    
    protected Using byDefault(String... keys) {
        virtualPart(keys, Include.DEFAULT);
        return using(Include.DEFAULT, keys);
    }
    
    protected Using noDepenencies() {
        return using(NO_DEPENDENCIES);
    }

    protected Using using() {
        return using(NO_DEPENDENCIES);
    }

    protected Using using(String... keys) {
        return using(Include.NOT_SPECIFIED, keys);
    }
    
    protected Using using(Include include, String... keys) {
        return new Using(include, keys);
    }

    public class Using {

        private final Include include;
        private final String[] required;

        public Using(Include include, String[] required) {
            this.include = include;
            this.required = required;
        }

        public Using select(String... selectClause) {
            if (include == Include.NOT_SPECIFIED) {
                select(required, selectClause);
            } else {
                select(required, include, selectClause);
            }
            return this;
        }

        public Using select(String selectClause) {
            if (include == Include.NOT_SPECIFIED) {
                select(required, selectClause);
            } else {
                select(required, include, selectClause);
            }
            return this;
        }

        public Using optional_select(String... selectClause) {
            optional_select(required, selectClause);
            return this;
        }

        public Using optional_select(String selectClause) {
            optional_select(required, selectClause);
            return this;
        }

        public Using internal_select(String... selectClause) {
            internal_select(required, selectClause);
            return this;
        }

        public Using internal_select(String selectClause) {
            internal_select(required, selectClause);
            return this;
        }

        public Using join(String join) {
            join(required, include, join);
            return this;
        }

        public Using join(String key, String join) {
            join(required, key, include, join);
            return this;
        }

        public Using where(String where) {
            where(required, include, where);
            return this;
        }

        public Using where(String key, String where) {
            where(required, key, include, where);
            return this;
        }
        
        public Using where(String... definitions) {
            where(required, include, definitions);
            return this;
        }

        public Using groupBy(String groupBy) {
            groupBy(required, include, groupBy);
            return this;
        }

        public Using groupBy(String key, String groupBy) {
            groupBy(required, key, include, groupBy);
            return this;
        }

        public Using having(String having) {
            having(required, include, having);
            return this;
        }

        public Using having(String key, String having) {
            having(required, key, include, having);
            return this;
        }

        public Using orderBy(String order) {
            orderBy(required, include, order);
            return this;
        }

        public Using orderBy(String key, String order) {
            orderBy(required, key, include, order);
            return this;
        }
    }
    
    public static abstract class PartTemplate<Entity> {
        protected final String key;
        protected final Include include;
        protected final String[] required;

        public PartTemplate(String key, Include include, String[] required) {
            this.key = key;
            this.include = include;
            this.required = required;
        }

        public abstract QueryPart createPart(String alias);
    }
    
    public static class SimplePartTemplate extends PartTemplate {
        private final String definition;
        private final PartType type;

        public SimplePartTemplate(PartType type, String key, Include include, String definition, String[] required) {
            super(key, include, required);
            this.definition = definition;
            this.type = type;
        }

        @Override
        public QueryPart createPart(String alias) {
            return new ParsingQueryBuilder.CustomPart(alias, type, definition);
        }
    }
    
    public static enum Include {
        NOT_SPECIFIED,
        OPTIONAL,
        DEFAULT,
        ALWAYS;
    }
    
    protected static final String[] AUTODETECT_DEPENDENCIES = null;
    protected static final String[] NO_DEPENDENCIES = {};
}
