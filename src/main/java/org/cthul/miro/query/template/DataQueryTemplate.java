package org.cthul.miro.query.template;

import java.util.*;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.*;
import org.cthul.miro.query.sql.DataQueryPart;
import static org.cthul.miro.query.template.Templates.*;

public class DataQueryTemplate extends SimpleQueryTemplate {
    
    private final DataQueryTemplateProvider provider;

    public DataQueryTemplate(DataQueryTemplateProvider provider, QueryTemplate parent) {
        super(parent);
        this.provider = provider;
    }
    
    protected Attribute getAttribute(String key) {
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
    
    public static class Parent extends DataQueryTemplate {

        public Parent(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            switch (key) {
                case "require-all-keys":
                    return new AttributeDependenciesTemplate(
                                    getKeyAttributes());
                case "keys-in":
                    return new WhereKeysInTemplate();
            }
            Attribute at = getAttribute(key);
            if (at != null) {
                return new AttributeTemplate(at);
            }

            String upper = key.toUpperCase();
            for (FilterOperator f: FILTERS) {
                if (f.matches(upper)) {
                    key = f.getAttributeName(key);
                    at = getAttribute(key);
                    if (at != null) {
                        return new WhereAttributeTemplate(at, f);
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
        protected QueryTemplatePart autoPart(String key) {
            switch (key) {
                case "all-keys":
                    return new PutAllTemplate(
                            getGeneratedKeys(), getNaturalKeys());
                case "*":
                    return new PutAllTemplate("all-keys", 
                            getDefaultAttributes(), getDefaultSelect());
                case "**":
                    return new PutAllTemplate("*", 
                            getOptionalAttributes(), getOptionalSelect());
                case "groupBy-keys":
                    List<String> groups = new ArrayList<>();
                    for (String k: getKeys()) {
                        groups.add("groupBy-"+ k);
                    }
                    return new PutAllTemplate(groups);
                case "orderBy-keys":
                    List<String> orders = new ArrayList<>();
                    for (String k: getKeys()) {
                        orders.add("orderBy-"+ k);
                    }
                    return new PutAllTemplate(orders);
            }
            if (key.startsWith("groupBy-")) {
                Attribute at = getAttribute(key.substring(8));
                if (at != null) {
                    return new GroupByTemplate(at);
                }
            }
            if (key.startsWith("orderBy-")) {
                String lower = key.toLowerCase();
                if (lower.endsWith(" asc")) {
                    return new Put2ValuesTemplate(
                            key.substring(0, key.length()-4), "asc");
                }
                if (lower.endsWith(" desc")) {
                    return new Put2ValuesTemplate(
                            key.substring(0, key.length()-5), "desc");
                }
                Attribute at = getAttribute(key.substring(8));
                if (at != null) {
                    return new OrderByTemplate(at);
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
        protected QueryTemplatePart autoPart(String key) {
            switch (key) {
                case "all-keys":
                    return new PutAllTemplate(getNaturalKeys());
                case "*":
                    return new PutAllTemplate("all-keys", getDefaultAttributes());
                case "**":
                    return new PutAllTemplate("*", getOptionalAttributes());
                case "insert-values":
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
        protected QueryTemplatePart autoPart(String key) {
            switch (key) {
                case "*":
                    return new PutAllTemplate(getDefaultAttributes());
                case "**":
                    return new PutAllTemplate("*", getOptionalAttributes());
                case "update-values":
                    return new UpdateValuesTemplate();
                case "filter-by-keys":
                    return new FilterByKeysTemplate();
            }
            if (key.startsWith("set-")) {
                Attribute at = getAttribute(key.substring(4));
                if (at != null) {
                    return new SetAttributeTemplate(at);
                }
            }
            return super.autoPart(key);
        }
    }
    
    public static class Delete extends DataQueryTemplate {

        public Delete(DataQueryTemplateProvider provider, QueryTemplate parent) {
            super(provider, parent);
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            switch (key) {
                case "*":
                case "**":
                    return new PutAllTemplate("require-all-keys");
                case "delete-values":
                    return new DeleteValuesTemplate();
                case "filter-by-keys":
                    return new FilterByKeysTemplate();
            }
            Attribute at = getAttribute(key);
            if (at != null) {
                return new AttributeDependenciesTemplate(at);
            }
            return super.autoPart(key);
        }
    }
    
    protected static class AttributeTemplate implements QueryTemplatePart {

        private final Attribute attribute;

        public AttributeTemplate(Attribute attribute) {
            this.attribute = attribute;
        }
        
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            attribute.addRequired(queryBuilder);
            QueryPart part = new SelectAttributePart(key, attribute);
            queryBuilder.addPart(DataQueryPart.ATTRIBUTE, part);
            return part;
        }
    }
    
    /**
     * SELECT t.c AS a
     * INTO t(c) (values/subquery)
     * SET c = ? (values)
     */
    protected static class SelectAttributePart extends PlainAttributePart implements SqlQueryPart {
        
        private String alias = null;

        public SelectAttributePart(String key, Attribute attribute) {
            super(key, attribute);
        }

        public String getAlias() {
            return alias != null ? alias : attribute.getKeyLiteral();
        }

        @Override
        public void put(String key, Object... args) {
            if ("AS".equals(key)) {
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
    
    protected static class SetAttributeTemplate implements QueryTemplatePart {
        
        private final Attribute attribute;

        public SetAttributeTemplate(Attribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            attribute.addRequired(queryBuilder);
            QueryPart part = new SetAttributePart(key, attribute);
            queryBuilder.addPart(DataQueryPart.ATTRIBUTE, part);
            return part;
        }
    }
    
    /**
     * SET a = ?
     */
    protected static class SetAttributePart extends AbstractQueryPart implements SqlQueryPart {
        
        private final Attribute attribute;
        private Object value = null;

        public SetAttributePart(String key, Attribute attribute) {
            super(key);
            this.attribute = attribute;
        }

        @Override
        public void put(String key, Object... args) {
            if ("".equals(key)) {
                value = args[0];
            } else {
                throw new IllegalArgumentException(key);
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
    
    protected static class InsertValuesTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            QueryPart part = new ValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class UpdateValuesTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put("filter-by-keys");
            QueryPart part = new ValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class DeleteValuesTemplate extends UpdateValuesTemplate {
        
    }
    
    protected static class ValuesPart extends VirtualQueryPart {
        
        private final InternalQueryBuilder queryBuilder;

        public ValuesPart(String key, InternalQueryBuilder queryBuilder) {
            super(key);
            this.queryBuilder = queryBuilder;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "add":
                    String k = queryBuilder.newKey("values");
                    queryBuilder.addPart(DataQueryPart.VALUES, new TuplePart(k, args));
                    return;
                default:
                    super.put(key, args);
            }
        }
    }
    
    protected class FilterByKeysTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
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
    
    protected class WhereKeysInTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put("require-all-keys");
            QueryPart part = new WhereKeysInPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class WhereKeysInPart implements SqlQueryPart {
        
        private final List<Object[]> values = new ArrayList<>();
        private final String key;

        public WhereKeysInPart(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "":
                    values.clear();
                case "addAll":
                    for (Object o: args) {
                        if (o instanceof List) {
                            values.add(((List) o).toArray());
                        } else {
                            values.add((Object[]) o);
                        }
                    }
                    break;
                case "add":
                    values.add(args);
                    break;
                default:
                    throw new IllegalArgumentException(key);
            }
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
            sqlBuilder.append('(');
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            for (Object[] v: values) {
                args.addAll(Arrays.asList(v));
            }
        }
    }
    
    protected class WhereKeysEqTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put("require-all-keys");
            QueryPart part = new WhereKeysInPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class WhereKeysEqPart implements SqlQueryPart {
        
        private final String key;
        private Object[] values = null;

        public WhereKeysEqPart(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "":
                    values = args;
                default:
                    throw new IllegalArgumentException(key);
            }
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
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            attribute.addRequired(queryBuilder);
            QueryPart part = new WhereAttributePart(key, attribute, filter);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected static class WhereAttributePart extends SimpleQueryPart {

        public WhereAttributePart(String key, Attribute attribute, FilterOperator filter) {
            super(key, attribute.getSelect() + filter.getSql());
        }
    }
    
    protected static class GroupByTemplate extends SimpleTemplatePart {

        private final Attribute at;
        
        public GroupByTemplate(Attribute at) {
            super(DataQueryPart.GROUP_BY, at.getSelect());
            this.at = at;
        }

        public GroupByTemplate(QueryPartType type, String sql, String... required) {
            super(type, sql, required);
            this.at = null;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            if (at != null) {
                at.addRequired(queryBuilder);
            }
            return super.addPart(key, queryBuilder);
        }
    }
    
    protected static class OrderByTemplate extends SimpleTemplatePart {

        private final Attribute at;
        
        public OrderByTemplate(Attribute at) {
            super(DataQueryPart.ORDER_BY, at.getSelect());
            this.at = at;
        }

        public OrderByTemplate(QueryPartType type, String sql, String... required) {
            super(type, sql, required);
            this.at = null;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            if (at != null) {
                at.addRequired(queryBuilder);
            }
            return super.addPart(key, queryBuilder);
        }

        @Override
        protected QueryPart createPart(String key, String sql) {
            return new OrderByPart(key, sql);
        }
    }
    
    protected static class OrderByPart extends SimpleQueryPart {
        
        private boolean asc = true;

        public OrderByPart(String key, String sql) {
            super(key, sql);
        }

        @Override
        public void put(String key, Object... args) {
            switch (key.toLowerCase()) {
                case "asc":
                    asc = true;
                    return;
                case "desc":
                    asc = false;
                    return;
                default:
                    super.put(key, args);
            }
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
        LE(         "<=",           " <= ?"),
        GE(         ">=",           " <= ?"),
        NE(         "<>",           " <> ?"),
        LT(         "<",            " < ?"),
        GT(         "<",            " > ?"),
        EQ(         "=",            " = ?");
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
