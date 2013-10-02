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
    
    protected PartTemplate addPart(PartTemplate pt) {
        parts.put(pt.key, pt);
        if (pt.include == Include.ALWAYS) {
            alwaysRequired.add(pt.key);
        } else if (pt.include == Include.DEFAULT) {
            defaultFields.add(pt.key);
        }
        return pt;
    }
    
    protected String[] copyDependencies(String[] source, int index) {
        if (source.length <= index || source[index] == null) {
            return NO_DEPENDENCIES;
        }
        return Arrays.copyOfRange(source, index, source.length);
    }
    
    protected List<PartTemplate> select(String... selectClause) {
        return select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> select(String selectClause) {
        return select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> select(String[] required, String... selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        for (String s: selectClause) {
            list.addAll(select(required, Include.DEFAULT, s));
        }
        return list;
    }
    
    protected List<PartTemplate> select(String[] required, Include include, String... selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        for (String s: selectClause) {
            list.addAll(select(required, include, s));
        }
        return list;
    }
    
    protected List<PartTemplate> select(String[] required, Include include, String selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate sp = new SimplePartTemplate(PartType.SELECT, part[0], include, part[1], req);
            list.add(select(sp));
        }
        return list;
    }
    
    protected PartTemplate select(String key, String[] required, String selectClause) {
        return select(key, required, Include.DEFAULT, selectClause);
    }
    
    protected PartTemplate select(String key, String[] required, Include include, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        if (selParts.length != 1) {
            throw new IllegalArgumentException("Expected one select: " + selectClause);
        }
        String[] part = selParts[0];
        String[] req;
        if (required == AUTODETECT_DEPENDENCIES) {
            req = copyDependencies(part, 2);
        } else {
            req = required;
        }
        PartTemplate sp = new SimplePartTemplate(PartType.SELECT, key, include, part[1], req);
        return select(sp);
    }
    
    protected List<PartTemplate> select(PartTemplate... selectParts) {
        final List<PartTemplate> list = new ArrayList<>();
        for (PartTemplate s: selectParts) {
            list.add(select(s));
        }
        return list;
    }
    
    protected PartTemplate select(PartTemplate selectPart) {
        selectableFields.add(selectPart.key);
        return addPart(selectPart);
    }
    
    protected List<PartTemplate> optional_select(String... selectClause) {
        return optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> optional_select(String selectClause) {
        return optional_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> optional_select(String[] required, String... selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        for (String s: selectClause) {
            list.addAll(select(required, Include.OPTIONAL, s));
        }
        return list;
    }
    
    protected List<PartTemplate> internal_select(String... selectClause) {
        return internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> internal_select(String selectClause) {
        return internal_select(AUTODETECT_DEPENDENCIES, selectClause);
    }
    
    protected List<PartTemplate> internal_select(String[] required, String... selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        for (String s: selectClause) {
            list.addAll(internal_select(required, Include.OPTIONAL, s));
        }
        return list;
    }
    
    protected List<PartTemplate> internal_select(String[] required, Include include, String selectClause) {
        final List<PartTemplate> list = new ArrayList<>();
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        for (String[] part: selParts) {
            String[] req;
            if (required == AUTODETECT_DEPENDENCIES) {
                req = copyDependencies(part, 2);
            } else {
                req = required;
            }
            PartTemplate sp = new SimplePartTemplate(PartType.SELECT_INTERNAL, part[0], include, part[1], req);
            list.add(internal_select(sp));
        }
        return list;
    }
    
    protected PartTemplate internal_select(String key, String[] required, String selectClause) {
        return internal_select(key, required, Include.OPTIONAL, selectClause);
    }
    
    protected PartTemplate internal_select(String key, String[] required, Include include, String selectClause) {
        String[][] selParts = SqlUtils.parseSelectClause(selectClause);
        if (selParts.length != 1) {
            throw new IllegalArgumentException("Expected one select: " + selectClause);
        }
        String[] part = selParts[0];
        String[] req;
        if (required == AUTODETECT_DEPENDENCIES) {
            req = copyDependencies(part, 2);
        } else {
            req = required;
        }
        PartTemplate sp = new SimplePartTemplate(PartType.SELECT, key, include, part[1], req);
        return internal_select(sp);
    }
    
    protected List<PartTemplate> internal_select(PartTemplate... selectParts) {
        final List<PartTemplate> list = new ArrayList<>();
        for (PartTemplate s: selectParts) {
            list.add(internal_select(s));
        }
        return list;
    }
    
    protected PartTemplate internal_select(PartTemplate selectPart) {
        return addPart(selectPart);
    }
    
    protected void from(String from) {
        String[] part = SqlUtils.parseFromPart(from);
        PartTemplate fp = new SimplePartTemplate(PartType.FROM, part[0], Include.ALWAYS, part[1], NO_DEPENDENCIES);
        from(fp);
    }
    
    protected void from(PartTemplate fp) {
        addPart(fp);
    }
    
    protected PartTemplate join(String join) {
        return join(NO_DEPENDENCIES, join);
    }
    
    protected PartTemplate join(String key, String join) {
        return join(NO_DEPENDENCIES, key, join);
    }
    
    protected PartTemplate join(String[] required, String join) {
        return join(required, Include.OPTIONAL, join);
   }

    protected PartTemplate join(String[] required, Include include, String join) {
        return join(required, DEFAULT_KEY, include, join);
    }
    
    protected PartTemplate join(String[] required, KeyMapper key, String join) {
        return join(required, key, Include.OPTIONAL, join);
    }
    
    protected PartTemplate join(String[] required, KeyMapper key, Include include, String join) {
        String[] part = SqlUtils.parseJoinPart(join);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        return join(required, key.map(part[0]), include, part[1]);
    }
    
    protected PartTemplate join(String[] required, String key, String join) {
        return join(required, key, Include.OPTIONAL, join);
    }
    
    protected PartTemplate join(String[] required, String key, Include include, String join) {
        PartTemplate jp = new SimplePartTemplate(PartType.JOIN, key, include, join, required);
        return join(jp);
    }
    
    protected PartTemplate join(PartTemplate jp) {
        return addPart(jp);
    }
    
    protected PartTemplate where(String where) {
        return where(NO_DEPENDENCIES, where);
    }
    
    protected List<PartTemplate> where(String... definitions) {
        return where(NO_DEPENDENCIES, definitions);
    }
    
    protected PartTemplate where(String key, String where) {
        return where(NO_DEPENDENCIES, key, where);
    }
    
    protected PartTemplate where(String[] required, String where) {
        return where(required, Include.OPTIONAL, where);
   }

    protected PartTemplate where(String[] required, Include include, String where) {
        String id = "$$where" + parts.size();
        return where(required, id, include, where);
    }
    
    protected List<PartTemplate> where(String[] required, String... definitions) {
        return where(required, Include.OPTIONAL, definitions);
    }
    
    protected List<PartTemplate> where(String[] required, Include include, String... definitions) {
        if (definitions.length % 2 != 0) {
            throw new IllegalArgumentException("Expected key-definition pairs");
        }
        final List<PartTemplate> list = new ArrayList<>();
        for (int i = 0; i < definitions.length; i += 2) {
            list.add(where(required, definitions[i], include, definitions[i+1]));
        }
        return list;
    }
    
    protected PartTemplate where(String[] required, String key, String where) {
        return where(required, key, Include.OPTIONAL, where);
    }
    
    protected PartTemplate where(String[] required, String key, Include include, String where) {
        PartTemplate wp = new SimplePartTemplate(PartType.WHERE, key, include, where, required);
        if (required == AUTODETECT_DEPENDENCIES) {
            throw new UnsupportedOperationException("AUTODETECT DEPENDENCIES");
        }
        return where(wp);
    }
    
    protected PartTemplate where(PartTemplate wp) {
        return addPart(wp);
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

    protected PartTemplate orderBy(String order) {
        return orderBy(NO_DEPENDENCIES, order);
    }
    
    protected PartTemplate orderBy(String key, String order) {
        return orderBy(NO_DEPENDENCIES, key, order);
    }
    
    protected List<PartTemplate> orderBy(String[] required, String[] order) {
        final List<PartTemplate> result = new ArrayList<>();
        for (String o: order) {
            result.add(orderBy(required, Include.OPTIONAL, o));
        }
        return result;
    }
    
    protected PartTemplate orderBy(String[] required, String order) {
        return orderBy(required, Include.OPTIONAL, order);
    }

    protected PartTemplate orderBy(String[] required, Include include, String order) {
        String id = "$$order" + parts.size();        
        return orderBy(required, id, include, order);
    }

    protected List<PartTemplate> orderBy(String[] required, KeyMapper key, String[] order) {
        final List<PartTemplate> result = new ArrayList<>();
        for (String o: order) {
            result.add(orderBy(required, key, Include.OPTIONAL, o));
        }
        return result;
    }
    
    protected PartTemplate orderBy(String[] required, KeyMapper key, String order) {
        return orderBy(required, key, Include.OPTIONAL, order);
    }
    
    protected PartTemplate orderBy(String[] required, String key, String order) {
        return orderBy(required, key, Include.OPTIONAL, order);
    }
    
    protected PartTemplate orderBy(String[] required, String key, Include include, String order) {
        PartTemplate jp = new SimplePartTemplate(PartType.ORDER, key, include, order, required);
        return orderBy(jp);
    }
    
    protected PartTemplate orderBy(String[] required, KeyMapper key, Include include, String order) {
        String column = SqlUtils.parseOrderPart(order)[0];
        PartTemplate jp = new SimplePartTemplate(PartType.ORDER, key.map(column), include, order, required);
        return orderBy(jp);
    }
    
    protected PartTemplate orderBy(PartTemplate gp) {
        return addPart(gp);
    }
    
    protected PartTemplate virtualPart(String[] required) {
        return virtualPart(required, Include.OPTIONAL);
    }
    
    protected PartTemplate virtualPart(String[] required, String key) {
        return virtualPart(required, Include.OPTIONAL, key);
    }
    
    protected PartTemplate virtualPart(String[] required, Include include) {
        return virtualPart(required, include, "$$virtual" + parts.size());
    }
    
    protected PartTemplate virtualPart(String[] required, Include include, String key) {
        PartTemplate pt = new SimplePartTemplate(PartType.VIRTUAL, key, include, "-virtual-part-", required);
        return addPart(pt);
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
    
    public static interface KeyMapper {
        String map(String key);
    }
    
    public static final KeyMapper DEFAULT_KEY = new KeyMapper() {
        @Override
        public String map(String key) { return key; }
    };
    
    public static class KeyPrefix implements KeyMapper {
        private final String prefix;

        public KeyPrefix(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String map(String key) {
            return prefix + key;
        }
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

        protected final Include include;
        protected final String[] required;

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
    
    public static abstract class PartTemplate {
        protected final String key;
        protected final Include include;
        protected final String[] required;

        public PartTemplate(String key, Include include, String[] required) {
            this.key = key;
            this.include = include;
            this.required = required;
        }

        public String getKey() {
            return key;
        }

        public abstract QueryPart createPart(String alias);

        @Override
        public String toString() {
            return "[" +  Arrays.toString(required) + "] -> " + key;
        }
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

        @Override
        public String toString() {
            return super.toString() + ": " + type + " " + definition;
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
