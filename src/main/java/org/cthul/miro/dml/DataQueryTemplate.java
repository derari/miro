package org.cthul.miro.dml;

import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.OtherQueryPart;
import java.util.*;
import org.cthul.miro.query.parts.*;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.query.template.Attribute;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplatePart;
import org.cthul.miro.query.template.AbstractQueryTemplate;
import static org.cthul.miro.query.template.Templates.*;
import static org.cthul.miro.dml.DataQueryKey.*;
import static org.cthul.miro.dml.DataQuerySubkey.*;

public class DataQueryTemplate extends AbstractQueryTemplate {
    
    private final DataQueryTemplateProvider provider;

    public DataQueryTemplate(DataQueryTemplateProvider provider, QueryTemplate parent) {
        super(parent);
        this.provider = provider;
    }
    
    protected Attribute getAttribute(String key) {
        return provider.getAttribute(key);
    }
    
    protected Attribute getAttribute(Object key) {
        if (!(key instanceof String)) return null;
        return getAttribute((String) key);
    }
    
    protected Attribute getSelectable(String key) {
        return provider.getAttribute(key);
    }
     
    protected List<String> getGeneratedKeys() {
        return provider.getGeneratedKeys();
    }
    
    protected List<String> getNaturalKeys() {
        return provider.getNaturalKeys();
    }
    
    protected List<String> getKeys() {
        return provider.getKeys();
    }
    
    protected List<String> getDefaultAttributes() {
        return provider.getDefaultAttributes();
    }
    
    protected List<String> getOptionalAttributes() {
        return provider.getOptionalAttributes();
    }
    
    protected List<String> getDefaultSelect() {
        return provider.getDefaultSelect();
    }
    
    protected List<String> getOptionalSelect() {
        return provider.getOptionalSelect();
    }
    
    protected List<Attribute> getKeyAttributes() {
        return provider.getKeyAttributes();
    }
    
    protected List<Object> getSelectParts(IncludeMode mode) {
        return provider.getSelectParts(mode);
    }
    
    protected List<Object> getInsertParts(IncludeMode mode) {
        return provider.getInsertParts(mode);
    }
    
    protected List<Object> getUpdateParts(IncludeMode mode) {
        return provider.getUpdateParts(mode);
    }
    
    protected List<Object> getDeleteParts(IncludeMode mode) {
        return provider.getDeleteParts(mode);
    }
    
    public static class Parent extends DataQueryTemplate {

        public Parent(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_KEYS:
                    return new PutAllTemplate(getGeneratedKeys(), getNaturalKeys());
                case ATTRIBUTE_DEPENDENCIES:
                    return new PutAttributeDependenciesTemplate();
                case INCLUDE_ATTRIBUTE:
                    return new PutAttributeTemplate();
                case ADD_TO_RESULT:
                    return new AddToResultTemplate();
                case ADD_GENERATED_KEYS_TO_RESULT:
                    return new Put2AllTemplate(ADD_TO_RESULT, getGeneratedKeys());
                case ALL_KEY_DEPENDENCIES:
                    return new Put2AllTemplate(ATTRIBUTE_DEPENDENCIES, getKeys());
                case KEYS_IN:
                    return new WhereKeysInTemplate();
            }
            Attribute at = getAttribute(key);
            if (at != null) {
                return new Put2ValuesTemplate(ATTRIBUTE, key);
            }
            if (key instanceof String) {
                String sKey = (String) key;
                int dot = sKey.indexOf('.');
                if (dot > 0) {
                    return new Put2AllTemplate(sKey.substring(0, dot), sKey.substring(dot+1));
                }
                String upper = sKey.toUpperCase();
                for (FilterOperator f: FILTERS) {
                    if (f.matches(upper)) {
                        key = f.getAttributeName(sKey);
                        at = getAttribute(key);
                        if (at != null) {
                            return new WhereAttributeTemplate(at, f);
                        }
                    }
                }
            }

            return super.autoPart(key);
        }
    }
    
    public static class Select extends DataQueryTemplate {

        public Select(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_ALWAYS:
                    return new PutAllTemplate(getSelectParts(IncludeMode.ALWAYS));
                case INCLUDE_DEFAULT:
                    return new PutAllTemplate(INCLUDE_KEYS, getDefaultAttributes(), getDefaultSelect(), getSelectParts(IncludeMode.DEFAULT));
                case INCLUDE_OPTIONAL:
                    return new PutAllTemplate(INCLUDE_DEFAULT, getOptionalAttributes(), getOptionalSelect(), getSelectParts(IncludeMode.OPTIONAL));
                case ATTRIBUTE:
                case SELECT:
                    return new ProxyTemplate(INCLUDE_ATTRIBUTE, ADD_TO_RESULT);
                case GROUP_BY:
                    return new GroupByTemplate();
                case GROUP_BY_KEYS:
                    return new Put2AllTemplate(GROUP_BY, getKeys());
                case ORDER_BY:
                    return new OrderByTemplate();
                case ORDER_BY_KEYS:
                    return new Put2AllTemplate(ORDER_BY, getKeys());
            }
            if (key instanceof String) {
                String sKey = (String) key;
                if (sKey.startsWith("groupBy-")) {
                    return new Put2AllTemplate(GROUP_BY, sKey.substring(8));
                }
                if (sKey.startsWith("orderBy-")) {
                    return new Put2AllTemplate(ORDER_BY, sKey.substring(8));
                }
            }
            return super.autoPart(key);
        }
    }
    
    public static class Insert extends DataQueryTemplate {

        public Insert(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_ALWAYS:
                    return new PutAllTemplate(getInsertParts(IncludeMode.ALWAYS));
                case INCLUDE_KEYS:
                    return new PutAllTemplate(getNaturalKeys());
                case INCLUDE_DEFAULT:
                    return new PutAllTemplate(INCLUDE_KEYS, getDefaultAttributes(), getInsertParts(IncludeMode.DEFAULT));
                case INCLUDE_OPTIONAL:
                    return new PutAllTemplate(INCLUDE_DEFAULT, getOptionalAttributes(), getInsertParts(IncludeMode.OPTIONAL));
                case ATTRIBUTE:
                case INSERT:
                    return new ProxyTemplate(INCLUDE_ATTRIBUTE);
                case VALUES:
                    return new ProxyTemplate(INSERT_VALUES);
                case INSERT_VALUES:
                    return new InsertValuesTemplate();
            }
            return super.autoPart(key);
        }
    }
    
    public static class Update extends DataQueryTemplate {

        public Update(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_ALWAYS:
                    return new PutAllTemplate(getUpdateParts(IncludeMode.ALWAYS));
                case INCLUDE_DEFAULT:
                    return new PutAllTemplate(getDefaultAttributes(), getUpdateParts(IncludeMode.DEFAULT));
                case INCLUDE_OPTIONAL:
                    return new PutAllTemplate(INCLUDE_DEFAULT, getOptionalAttributes(), getUpdateParts(IncludeMode.OPTIONAL));
                case ATTRIBUTE:
                case UPDATE:
                    return new ProxyTemplate(INCLUDE_ATTRIBUTE);
                case VALUES:
                case UPDATE_VALUES:
                    return new UpdateValuesTemplate();
                case FILTER_BY_KEYS:
                    return new FilterByKeysTemplate();
                case SET_ATTRIBUTE:
                    return new SetAttributeTemplate();
            }
            return super.autoPart(key);
        }
    }
    
    public static class Delete extends DataQueryTemplate {

        public Delete(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_ALWAYS:
                    return new PutAllTemplate(getDeleteParts(IncludeMode.ALWAYS));
                case INCLUDE_DEFAULT:
                    return new PutAllTemplate(FILTER_BY_KEYS, getDeleteParts(IncludeMode.DEFAULT));
                case INCLUDE_OPTIONAL:
                    return new PutAllTemplate(INCLUDE_DEFAULT, getDeleteParts(IncludeMode.OPTIONAL));
                case ATTRIBUTE:
                case DELETE:
                    return new NoAttributesTemplate();
                case VALUES:
                case DELETE_VALUES:
                    return new DeleteValuesTemplate();
                case FILTER_BY_KEYS:
                    return new FilterByKeysTemplate();
            }
            Attribute at = getAttribute(key);
            if (at != null) {
                return new AttributeDependenciesTemplate(at);
            }
            return super.autoPart(key);
        }
    }
    
    protected class AddToResultTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_DEFAULT:
                case INCLUDE_OPTIONAL:
                    // includes are handled by INCLUDE_ATTRIBUTE
                    return true;
            }
            if (!(key instanceof String)) return false;
            Attribute at = getAttribute((String) key);
            if (at == null) return false;
            queryBuilder.addResultAttribute((String) key);
            return true;
        }
    }
    
    protected class PutAttributeDependenciesTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            if (!(key instanceof String)) return false;
            Attribute at = getAttribute((String) key);
            if (at == null) return false;
            at.addRequired(queryBuilder);
            return true;
        }
    }
    
    protected class PutAttributeTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_DEFAULT:
                case INCLUDE_OPTIONAL:
                    queryBuilder.put(key);
                    return true;
            }
            if (!(key instanceof String)) return false;
            Attribute at = getAttribute((String) key);
            if (at == null) return false;
            queryBuilder.put2(ATTRIBUTE_DEPENDENCIES, key);
            queryBuilder.addPart(DataQueryPart.ATTRIBUTE, new SelectAttributePart(newKey, at));
            return true;
        }
    }
    
    protected class NoAttributesTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            switch (asDataQueryKey(key)) {
                case INCLUDE_DEFAULT:
                case INCLUDE_OPTIONAL:
                    queryBuilder.put(key);
                    return true;
            }
            throw new IllegalArgumentException(
                    "Cannot delete attribute: " + key);
//            if (!(key instanceof String)) return false;
//            Attribute at = getAttribute((String) key);
//            if (at == null) return false;
//            queryBuilder.put2(ATTRIBUTE_DEPENDENCIES, key);
//            queryBuilder.addPart(DataQueryPart.ATTRIBUTE, new SelectAttributePart(newKey, at));
//            return true;
        }
    }
    
    /**
     * ATTRIBUTE t.c AS a
     * INTO t(c) (values/subquery)
     * SET c = ? (values)
     */
    protected static class SelectAttributePart extends PlainAttributePart implements SqlQueryPart {
        
        private String alias = null;

        public SelectAttributePart(Object key, Attribute attribute) {
            super(key, attribute);
        }

        public String getAlias() {
            return alias != null ? alias : attribute.getKeyLiteral();
        }

        @Override
        public void put(Object key, Object... args) {
            if (asDataQuerySubkey(key) == AS) {
                alias = (String) args[0];
            } else {
                super.put(key, args);
            }
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(attribute.getSelect());
            sqlBuilder.append(" AS ");
            sqlBuilder.append(getAlias());
        }

        @Override
        public void appendArgsTo(List<Object> args) {
        }
    }
    
    protected class SetAttributeTemplate extends ConfigurationTemplate {

        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            if (!(key instanceof String)) return false;
            Attribute at = getAttribute((String) key);
            if (at == null) return false;
            QueryPart qp = new SetAttributePart(newKey, at);
            qp.put(null, args);
            queryBuilder.addPart(DataQueryPart.ATTRIBUTE, qp);
            return true;
        }
    }
    
    /**
     * SET a = ?
     */
    protected static class SetAttributePart extends AbstractQueryPart implements SqlQueryPart {
        
        private final Attribute attribute;
        private Object value = null;

        public SetAttributePart(Object key, Attribute attribute) {
            super(key);
            this.attribute = attribute;
        }

        @Override
        public void put(Object key, Object... args) {
            if (asDataQuerySubkey(key) == DEFAULT) {
                value = args[0];
            } else {
                super.put(key, args);
            }
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            sqlBuilder.append(attribute.getColumnLiteral());
            sqlBuilder.append(" = ?");
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            args.add(value);
        }
    }
    
    protected class AddValuesTemplate extends ConfigurationTemplate {
        public AddValuesTemplate(Object... required) {
            super(required);
        }

        @Override
        protected boolean handlePut(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... args) {
            switch (asDataQuerySubkey(key)) {
                case ADD:
                    Object k = queryBuilder.newKey("values");
                    queryBuilder.addPart(DataQueryPart.VALUES, new TuplePart(k, args));
                    return true;
                default:
                    return false;
            }
        }
    }
    
    protected class InsertValuesTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (String gk: getGeneratedKeys()) {
                queryBuilder.addResultAttribute(gk);
            }
            QueryPart part = new ValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class UpdateValuesTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(FILTER_BY_KEYS);
            QueryPart part = new ValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class DeleteValuesTemplate extends UpdateValuesTemplate {
        
    }
    
    protected static class ValuesPart extends VirtualQueryPart {
        
        private final InternalQueryBuilder queryBuilder;

        public ValuesPart(Object key, InternalQueryBuilder queryBuilder) {
            super(key);
            this.queryBuilder = queryBuilder;
        }

        @Override
        public void put(Object key, Object... args) {
            switch (asDataQuerySubkey(key)) {
                case ADD:
                    Object k = queryBuilder.newKey("values");
                    queryBuilder.addPart(DataQueryPart.VALUES, new TuplePart(k, args));
                    return;
            }
            super.put(key, args);
        }
    }
    
    protected class FilterByKeysTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (String s: getKeys()) {
                addAttributeFilter(s, queryBuilder);
            }
            return virtualPart(key, queryBuilder);
        }
        
        public void addAttributeFilter(String key, InternalQueryBuilder queryBuilder) {
            Attribute at = getAttribute(key);
            at.addRequired(queryBuilder);
            AttributeQueryPart aPart = new PlainAttributePart(key, at);
            queryBuilder.addPart(DataQueryPart.FILTER_ATTRIBUTE, aPart);
        }
    }
    
    protected class WhereKeysInTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(ALL_KEY_DEPENDENCIES);
            QueryPart part = new WhereKeysInPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class WhereKeysInPart extends AbstractQueryPart implements SqlQueryPart {
        
        private final List<Object[]> values = new ArrayList<>();

        public WhereKeysInPart(Object key) {
            super(key);
        }

        @Override
        public void put(Object key, Object... args) {
            switch (asDataQuerySubkey(key)) {
                case DEFAULT:
                    values.clear();
                case ADD_ALL:
                    for (Object o: args) {
                        if (o instanceof List) {
                            values.add(((List) o).toArray());
                        } else if (o instanceof Object[]) {
                            values.add((Object[]) o);
                        } else {
                            values.add(new Object[]{o});
                        }
                    }
                    return;
                case ADD:
                    values.add(args);
                    return;
            }
            super.put(key, args);
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            String atString = sqlKeysEq(provider);
            sqlBuilder.append('(');
            
            int len = values.size();
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    sqlBuilder.append(" OR ");
                }
                sqlBuilder.append(atString);
            }
            sqlBuilder.append(')');
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            for (Object[] v: values) {
                args.addAll(Arrays.asList(v));
            }
        }
    }
    
    protected class WhereKeysEqTemplate extends AbstractTemplatePart {
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(ALL_KEY_DEPENDENCIES);
            QueryPart part = new WhereKeysEqPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class WhereKeysEqPart extends AbstractQueryPart implements SqlQueryPart {
        
        private Object[] values = null;

        public WhereKeysEqPart(Object key) {
            super(key);
        }

        @Override
        public void put(Object key, Object... args) {
            if (key == null || "".equals(key)) {
                values = args;
                return;
            }
            super.put(key, args);
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            String atString = sqlKeysEq(provider);
            sqlBuilder.append(atString);
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            args.addAll(Arrays.asList(values));
        }
    }
    
    protected static class WhereAttributeTemplate implements QueryTemplatePart {

        private final Attribute attribute;
        private final FilterOperator filter;

        public WhereAttributeTemplate(Attribute attribute, FilterOperator filter) {
            this.attribute = attribute;
            this.filter = filter;
        }
        
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            attribute.addRequired(queryBuilder);
            QueryPart part = new WhereAttributePart(key, attribute, filter);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected static class WhereAttributePart extends SimpleQueryPart {

        public WhereAttributePart(Object key, Attribute attribute, FilterOperator filter) {
            super(key, attribute.getSelect() + filter.getSql());
        }
    }
    
    protected class GroupByTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            Attribute at = getAttribute(key);
            if (at == null) return false;
            at.addRequired(queryBuilder);
            queryBuilder.addPart(DataQueryPart.GROUP_BY, new SimpleQueryPart(newKey, at.getSelect()));
            return true;
        }
    }
    
    protected class OrderByTemplate extends ConfigurationTemplate {
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            DataQuerySubkey ordering = null;
            if (key instanceof String) {
                String sKey = (String) key;
                String lower = sKey.toLowerCase();
                if (lower.endsWith(" asc") || lower.endsWith(".asc")) {
                    key = sKey.substring(0, sKey.length()-4);
                    ordering = ASC;
                } else if (lower.endsWith(" desc") || lower.endsWith(".desc")) {
                    key = sKey.substring(0, sKey.length()-5);
                    ordering = DESC;
                }
            }
            Attribute at = getAttribute(key);
            if (at == null) return false;
            at.addRequired(queryBuilder);
            queryBuilder.addPart(DataQueryPart.ORDER_BY, new OrderByPart(newKey, at.getSelect()));
            if (ordering != null) {
                queryBuilder.put2(newKey, ordering);
            }
            return true;
        }
    }
    
    protected static class OrderByPart extends SimpleQueryPart {
        
        private boolean asc = true;

        public OrderByPart(Object key, String sql) {
            super(key, sql);
        }

        @Override
        public void put(Object key, Object... args) {
            switch(asDataQuerySubkey(key)) {
                case ASC:
                    asc = true;
                    return;
                case DESC:
                    asc = false;
                    return;
            }
            super.put(key, args);
        }

        @Override
        public void appendSqlTo(StringBuilder sqlBuilder) {
            super.appendSqlTo(sqlBuilder);
            if (asc) {
                sqlBuilder.append(" ASC");
            } else {
                sqlBuilder.append(" DESC");
            }
        }
    }
    
    private static final FilterOperator[] FILTERS = FilterOperator.values();
    
    protected static enum FilterOperator {
        
        IS_NOT_NULL(" IS NOT NULL", " IS NOT NULL", 0),
        NOT_NULL(   " NOT NULL",    " IS NOT NULL", 0),
        IS_NULL(    " IS NULL",     " IS NULL",     0),
        BETWEEN(    " BETWEEN",     " BETWEEN ? AND ?", 2),
        LIKE(       " LIKE",        " LIKE ?"),
        LE(         "<=",           " <= ?"),
        GE(         ">=",           " <= ?"),
        NE(         "<>",           " <> ?"),
        EQ(         "=",            " = ?"),
        LT(         "<",            " < ?"),
        GT(         "<",            " > ?");
        // TODO IN("IN", " IN ?") with auto-expansion for array params
        
        private final String key;
        private final String sql;
        private final int argC;

        private FilterOperator(String key, String sql, int argC) {
            this.key = key;
            this.sql = sql;
            this.argC = argC;
        }

        private FilterOperator(String key, String sql) {
            this(key,sql,1);
        }

        public String getKey() {
            return key;
        }

        public String getSql() {
            return sql;
        }

        public int getArgC() {
            return argC;
        }
        
        public boolean matches(String key) {
            return key.endsWith(getKey());
        }
        
        public String getAttributeName(String key) {
            return key.substring(0, key.length() - getKey().length()).trim();
        }
    }

    protected static String sqlKeysEq(DataQueryTemplateProvider template) {
        StringBuilder atString = new StringBuilder();
        atString.append('(');
        for (Attribute a: template.getKeyAttributes()) {
            if (atString.length() > 1) {
                atString.append(" AND ");
            }
            atString.append(a.getSelect());
            atString.append(" = ?");
        }
        return atString.append(')').toString();
    }
}
