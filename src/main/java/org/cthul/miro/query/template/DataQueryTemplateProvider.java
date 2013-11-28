package org.cthul.miro.query.template;

import org.cthul.miro.query.parts.SimpleQueryPart;
import java.util.*;
import org.cthul.miro.doc.AutoDependencies;
import org.cthul.miro.doc.AutoKey;
import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.AbstractQueryPart;
import org.cthul.miro.query.parts.AttributeQueryPart;
import org.cthul.miro.query.parts.InsertTuplePart;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.parts.UpdateTuplePart;
import org.cthul.miro.query.parts.VirtualQueryPart;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.util.SqlUtils;
import static org.cthul.miro.query.sql.DataQuery.Type.*;

public class DataQueryTemplateProvider implements QueryTemplateProvider {
    
    private final QueryTemplate select;
    private final QueryTemplate insert;
    private final QueryTemplate update;
    private final QueryTemplate delete;
    
    private final Map<String, QueryTemplatePart> parts = new HashMap<>();
    private final Map<String, Attribute> attributes = new HashMap<>();
    private final List<String> generatedKeys = new ArrayList<>();
    private final List<String> naturalKeys = new ArrayList<>();
    private final List<String> defaultAttributes = new ArrayList<>();
    private final List<String> optionalAttributes = new ArrayList<>();
    private final List<String> defaultSelect = new ArrayList<>();
    private final List<String> optionalSelect = new ArrayList<>();

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DataQueryTemplateProvider() {
        select = newSelect();
        insert = newInsert();
        update = newUpdate();
        delete = newDelete();
    }
    
    protected QueryTemplate newSelect() {
        return new Select(this);
    }

    protected QueryTemplate newInsert() {
        return new Insert(this);
    }

    protected QueryTemplate newUpdate() {
        return new Update(this);
    }

    protected QueryTemplate newDelete() {
        return new Delete(this);
    }

    @Override
    public QueryTemplate getTemplate(QueryType<?> queryType) {
        switch (DataQuery.Type.get(queryType)) {
            case SELECT:
                return select;
            case INSERT:
                return insert;
            case UPDATE:
                return update;
            case DELETE:
                return delete;
        }
        return null;
    }
    
    protected synchronized void add(String key, QueryTemplatePart part) {
        parts.put(key, part);
    }
    
    protected synchronized void addIfNew(String key, QueryTemplatePart part) {
        if (parts.containsKey(key)) return;
        parts.put(key, part);
    }
    
    protected void addAs(QueryTemplatePart part, String... keys) {
        for (String k: keys) {
            add(k, part);
        }
    }
    
    protected List<Attribute> getAttributes(List<String>... keys) {
        List<Attribute> result = new ArrayList<>();
        for (List<String> list: keys) {
            for (String s: list) {
                result.add(attributes.get(s));
            }
        }
        return result;
    }
    
    protected String newTemplatePartKey(String hint) {
        hint += "$";
        if (!parts.containsKey(hint)) {
            return hint;
        }
        int i = 1;
        while (parts.containsKey(hint + i)) i++;
        return hint + i;
    }
    
    protected QueryTemplatePart autoPartSelect(String key) {
        switch (key) {
            case "all-keys":
                return new RequireAllTemplate(
                        generatedKeys, naturalKeys);
            case "*":
                return new RequireAllTemplate("all-keys", 
                        defaultAttributes, defaultSelect);
            case "**":
                return new RequireAllTemplate("*", 
                        optionalAttributes, optionalSelect);
            case "groupBy-keys":
                List<String> groups = new ArrayList<>();
                for (String k: generatedKeys) {
                    groups.add("groupBy-"+ k);
                }
                for (String k: naturalKeys) {
                    groups.add("groupBy-"+ k);
                }
                return new RequireAllTemplate(groups);
            case "orderBy-keys":
                List<String> orders = new ArrayList<>();
                for (String k: generatedKeys) {
                    orders.add("orderBy-"+ k);
                }
                for (String k: naturalKeys) {
                    orders.add("orderBy-"+ k);
                }
                return new RequireAllTemplate(orders);
        }
        if (key.startsWith("groupBy-")) {
            Attribute at = attributes.get(key.substring(8));
            if (at != null) {
                return new GroupByTemplate(at);
            }
        }
        if (key.startsWith("orderBy-")) {
            if (key.toLowerCase().endsWith(" asc")) {
                return new Put2ValuesTemplate(
                        key.substring(0, key.length()-4), "asc");
            }
            if (key.toLowerCase().endsWith(" desc")) {
                return new Put2ValuesTemplate(
                        key.substring(0, key.length()-5), "desc");
            }
            Attribute at = attributes.get(key.substring(8));
            if (at != null) {
                return new OrderByTemplate(at);
            }
        }
        return autoPartCached(key);
    }
    
    protected QueryTemplatePart autoPartInsert(String key) {
        switch (key) {
            case "all-keys":
                return new RequireAllTemplate(naturalKeys);
            case "*":
                return new RequireAllTemplate("all-keys", defaultAttributes);
            case "**":
                return new RequireAllTemplate("*", optionalAttributes);
            case "insert-values":
                return new InsertValuesTemplate();
        }
        return autoPartCached(key);
    }
    
    protected QueryTemplatePart autoPartUpdate(String key) {
        switch (key) {
            case "all-keys":
                return new RequireAttributeTemplate(
                        getAttributes(generatedKeys, naturalKeys));
            case "*":
                return new RequireAllTemplate(defaultAttributes);
            case "**":
                return new RequireAllTemplate("*", optionalAttributes);
            case "update-values":
                return new UpdateValuesTemplate();
            case "filter-by-keys":
                return new FilterByKeysTemplate();
        }
        if (key.startsWith("set-")) {
            Attribute at = attributes.get(key.substring(4));
            if (at != null) {
                return new SetAttributeTemplate(at);
            }
        }
        return autoPartCached(key);
    }
    
    protected QueryTemplatePart autoPartDelete(String key) {
        switch (key) {
            case "all-keys":
                return new RequireAllTemplate(
                        generatedKeys, naturalKeys);
            case "*":
            case "**":
                return new RequireAllTemplate("all-keys");
        }
        Attribute at = attributes.get(key);
        if (at != null) {
            return new RequireAttributeTemplate(at);
        }
        return autoPartCached(key);
    }
    
    private QueryTemplatePart autoPartCached(String key) {
        QueryTemplatePart part = parts.get(key);
        if (part != null) return part;
        part = autoPart(key);
        if (part == null) return null;
        add(key, part);
        return part;
    }
    
    protected QueryTemplatePart autoPart(String key) {
        switch (key) {
            case "keys-in":
                return new KeysInTemplate();
        }
        Attribute at = attributes.get(key);
        if (at != null) {
            return new SelectAttributeTemplate(at);
        }
        return null;
    }
    
    protected String[] getRequired(String[] required, String[] part) {
        if (required != AUTO_DEPENDENCIES) {
            return required;
        }
        String table = part[2] == null ? "main-table" : part[2];
        if (part[3] == null) {
            return new String[]{table};
        } else {
            return new String[]{table + "." + part[3]};
        }
    }
    
    protected List<Attribute> newAttributes(String[] required, String sql) {
        String[][] attributeParts = SqlUtils.parseAttributes(sql);
        List<Attribute> result = new ArrayList<>(attributeParts.length);
        for (String[] p: attributeParts) {
            required = getRequired(required, p);
            Attribute a = new Attribute(p[0], p[1], p[4], p[5], required);
            result.add(a);
            attributes.put(a.getKey(), a);
        }
        return result;
    }
    
    protected List<Attribute> newSelects(String[] required, String sql) {
        String[][] attributeParts = SqlUtils.parseSelectClause(sql);
        List<Attribute> result = new ArrayList<>(attributeParts.length);
        for (String[] p: attributeParts) {
            required = getRequired(required, p);
            Attribute a = new Attribute(p[0], p[1], p[4], null, required);
            result.add(a);
            attributes.put(a.getKey(), a);
        }
        return result;
    }
    
    protected void addAttributeKeysTo(Iterable<Attribute> attributes, Collection<String> keys) {
        for (Attribute a: attributes) {
            keys.add(a.getKey());
        }
    }
    
    protected void addAttributesTo(Iterable<Attribute> attributes, Map<String, Attribute> map) {
        for (Attribute a: attributes) {
            map.put(a.getKey(), a);
        }
    }
    
    protected void generatedKeys(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        generatedKeys(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void generatedKeys(String[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, generatedKeys);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void naturalKeys(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        naturalKeys(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void naturalKeys(String[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, naturalKeys);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void attributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        attributes(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void attributes(String[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, defaultAttributes);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void optionalAttributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        optionalAttributes(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void optionalAttributes(String[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, optionalAttributes);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void select(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        select(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void select(String[] required, @MultiValue @AutoKey String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(required, s);
            addAttributeKeysTo(list, defaultSelect);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void optionalSelect(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        optionalSelect(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void optionalSelect(String[] required, @MultiValue @AutoKey String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(required, s);
            addAttributeKeysTo(list, optionalSelect);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void table(@AutoKey String table) {
        String[] p = SqlUtils.parseFromPart(table);
        TableTemplate tt = new TableTemplate(p[0], p[1], p[2]);
        addIfNew(tt.getKey(), tt);
        addIfNew("main-table", new RequireAllTemplate(tt.getKey()));
        // TODO: find out how to handle multiple tables and/or tables with no key
    }
    
    protected void join(@AutoKey String join) {
        String[] p = SqlUtils.parseJoinPart(join);
        join(p[0], p[1]);
    }
    
    protected void join(String key, String join) {
        JoinTemplate jt = new JoinTemplate(join);
        add(key, jt);
    }
    
    protected void virtual(String[] required, String key) {
        RequireAllTemplate vt = new RequireAllTemplate(required);
        add(key, vt);
    }
    
    protected Using<?> using(String... required) {
        return new Using<>(this, required);
    }
    
    protected String[] dependenciesForUsing(String[] required) {
        if (required == AUTO_DEPENDENCIES || required == NO_DEPENDENCIES) {
            return required;
        }
        String key;
        if (required != null && required.length > 0) {
            StringBuilder sb = new StringBuilder();
            int l = Math.min(3, required.length);
            for (int i = 0; i < l; i++) {
                if (sb.length() > 0) sb.append('+');
                sb.append(required[i].replace('.', '~'));
            }
            if (required.length > 3) sb.append("+").append(required.length-3);
            key = sb.toString();
        } else {
            key = "virtual";
        }
        key = newTemplatePartKey(key);
        virtual(required, key);
        return new String[]{key};
    }

    protected static class SelectAttributeTemplate implements QueryTemplatePart {

        private final Attribute attribute;

        public SelectAttributeTemplate(Attribute attribute) {
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
    protected static class SelectAttributePart extends AbstractQueryPart implements AttributeQueryPart {
        
        private final Attribute attribute;
        private String alias = null;

        public SelectAttributePart(String key, Attribute attribute) {
            super(key);
            this.attribute = attribute;
        }

        @Override
        public String getAttributeKey() {
            return attribute.getKey();
        }

        @Override
        public String getSqlName() {
            return attribute.getColumnLiteral();
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
    protected static class SetAttributePart extends AbstractQueryPart implements QueryPart {
        
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
    
    protected static class RequireAttributeTemplate implements QueryTemplatePart {
        
        private final List<Attribute> attributes;

        public RequireAttributeTemplate(Attribute... attributes) {
            this.attributes = Arrays.asList(attributes);
        }
        
        public RequireAttributeTemplate(List<Attribute>... attributes) {
            this.attributes = new ArrayList<>();
            for (List<Attribute> a: attributes) {
                this.attributes.addAll(a);
            }
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            for (Attribute a: attributes) {
                a.addRequired(queryBuilder);
            }
            return virtualPart(key, queryBuilder);
        }
    }
    
    protected class RequireAllTemplate implements QueryTemplatePart {
        
        private final List<String> keys;

        public RequireAllTemplate(String key) {
            this.keys = new ArrayList<>();
            if (key != null) keys.add(key);
        }
        
        public RequireAllTemplate(String... keys) {
            this(Arrays.asList(keys));
        }
        
        public RequireAllTemplate(String oneMore, List<String>... keys) {
            this(oneMore);
            for (List<String> k: keys) {
                this.keys.addAll(k);
            }
        }
        
        public RequireAllTemplate(List<String>... keys) {
            this(null, keys);
        }
        
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, keys);
            return proxyPart(key, queryBuilder, keys);
        }
    }
    
    protected static class TableTemplate implements QueryTemplatePart {
        
        private final String key;
        private final String from;
        private final String table;

        public TableTemplate(String key, String from, String table) {
            this.key = key;
            this.from = from;
            this.table = table;
        }

        public String getKey() {
            return key;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            String sql = null;
            switch (DataQuery.Type.get(queryBuilder.getQueryType())) {
                case SELECT:
                case DELETE:
                case UPDATE:
                    sql = from;
                    break;
                case INSERT:
                    sql = table;
                    break;
            }
            if (sql == null) return null;
            QueryPart part = new TableQueryPart(key, sql);
            queryBuilder.addPart(DataQueryPart.TABLE, part);
            return part;
        }
    }
    
    protected static class TableQueryPart extends SimpleQueryPart {

        public TableQueryPart(String key, String sql) {
            super(key, sql);
        }

        @Override
        public void put(String key, Object... args) {
            if (args != null && args.length > 0) {
                super.put(key, args);
            }
        }
    }
    
    protected static class InsertValuesTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            QueryPart part = new InsertValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class InsertValuesPart extends VirtualQueryPart {
        
        private final InternalQueryBuilder queryBuilder;

        public InsertValuesPart(String key, InternalQueryBuilder queryBuilder) {
            super(key);
            this.queryBuilder = queryBuilder;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "add":
                    String k = queryBuilder.newKey("values");
                    queryBuilder.addPart(DataQueryPart.VALUES, new InsertTuplePart(k, args));
                    return;
                default:
                    super.put(key, args);
            }
        }
    }
    
    protected static class UpdateValuesTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put("filter-by-keys");
            QueryPart part = new UpdateValuesPart(key, queryBuilder);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
            return part;
        }
    }
    
    protected static class UpdateValuesPart extends VirtualQueryPart {
        
        private final InternalQueryBuilder queryBuilder;

        public UpdateValuesPart(String key, InternalQueryBuilder queryBuilder) {
            super(key);
            this.queryBuilder = queryBuilder;
        }

        @Override
        public void put(String key, Object... args) {
            switch (key) {
                case "add":
                    String k = queryBuilder.newKey("values");
                    queryBuilder.addPart(DataQueryPart.VALUES, new UpdateTuplePart(k, args));
                    return;
                default:
                    super.put(key, args);
            }
        }
    }
    
    protected static class JoinTemplate implements QueryTemplatePart {

        private final String sql;
        private final String[] required;

        public JoinTemplate(String sql, String... required) {
            this.sql = sql;
            this.required = required;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, required);
            TableQueryPart jp = new TableQueryPart(key, sql);
            queryBuilder.addPart(DataQueryPart.JOIN, jp);
            return jp;
        }
    }
    
    protected class KeysInTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put("all-keys");
            QueryPart part = new KeysInPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class KeysInPart implements QueryPart {
        
        private final List<Object[]> values = new ArrayList<>();
        private final String key;

        public KeysInPart(String key) {
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
            String atString = sqlKeysEq(DataQueryTemplateProvider.this);
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
    
    protected class KeysEqTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            QueryPart part = new KeysInPart(key);
            queryBuilder.addPart(DataQueryPart.WHERE, part);
            return part;
        }
    }
    
    protected class KeysEqPart implements QueryPart {
        
        private Object[] values = null;
        private final String key;

        public KeysEqPart(String key) {
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
            String atString = sqlKeysEq(DataQueryTemplateProvider.this);
            sqlBuilder.append(atString);
        }

        @Override
        public void appendArgsTo(List<Object> args) {
            args.addAll(Arrays.asList(values));
        }
    }
    
    protected class FilterByKeysTemplate implements QueryTemplatePart {

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            for (String s: generatedKeys) {
                addAttributeFilter(s, queryBuilder);
            }
            for (String s: naturalKeys) {
                addAttributeFilter(s, queryBuilder);
            }
            return virtualPart(key, queryBuilder);
        }
        
        public void addAttributeFilter(String key, InternalQueryBuilder queryBuilder) {
            Attribute at = attributes.get(key);
            AttributeQueryPart aPart = new SelectAttributePart(key, at);
            queryBuilder.addPart(DataQueryPart.WHERE, aPart);
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
    
    protected static class PutValuesTemplate implements QueryTemplatePart {
        private final String key;
        private final Object[] values;

        public PutValuesTemplate(String key, Object... values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(this.key, values);
            return virtualPart(key, queryBuilder);
        }
    }
    
    protected static class Put2ValuesTemplate implements QueryTemplatePart {
        private final String key;
        private final String key2;
        private final Object[] values;

        public Put2ValuesTemplate(String key, String key2, Object... values) {
            this.key = key;
            this.key2 = key2;
            this.values = values;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put2(this.key, key2, values);
            return virtualPart(key, queryBuilder);
        }
    }
    
    protected static final String[] AUTO_DEPENDENCIES = null;
    protected static final String[] NO_DEPENDENCIES = {};
    
    protected static class Using<This extends Using<? extends This>> {
        
        private final DataQueryTemplateProvider template;
        private final String[] required;

        public Using(DataQueryTemplateProvider template, String... required) {
            this.template = template;
            this.required = template.dependenciesForUsing(required);
        }
        
        protected This self() {
            return (This) this;
        }
        
        public This select(@MultiValue @AutoKey String... select) {
            template.select(required, select);
            return self();
        }
        
        public This optionalSelect(@MultiValue @AutoKey String... select) {
            template.optionalSelect(required, select);
            return self();
        }
    }
    
    protected static void requireAll(InternalQueryBuilder queryBuilder, String... required) {
        for (String s: required) {
            queryBuilder.put(s);
        }
    }
    
    protected static void requireAll(InternalQueryBuilder queryBuilder, List<String> required) {
        for (String s: required) {
            queryBuilder.put(s);
        }
    }
    
    protected static String sqlKeysEq(DataQueryTemplateProvider template) {
        StringBuilder atString = new StringBuilder();
        atString.append('(');
        for (String s: template.generatedKeys) {
            Attribute a = template.attributes.get(s);
            if (atString.length() > 1) {
                atString.append(" AND ");
            }
            atString.append(a.getSelect());
            atString.append(" = ?");
        }
        for (String s: template.naturalKeys) {
            Attribute a = template.attributes.get(s);
            if (atString.length() > 1) {
                atString.append(" AND ");
            }
            atString.append(a.getSelect());
            atString.append(" = ?");
        }
        return atString.append(')').toString();
    }
    
    protected static QueryPart virtualPart(String key, InternalQueryBuilder queryBuilder) {
        QueryPart part = new VirtualQueryPart(key);
        queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
        return part;
    }
    
    protected static QueryPart proxyPart(String key, final InternalQueryBuilder queryBuilder, final List<String> required) {
        QueryPart part = new VirtualQueryPart(key) {
            @Override
            public void put(String key, Object... args) {
                for (String r: required) {
                    queryBuilder.put2(r, key, args);
                }
            }
        };
        queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
        return part;
    }
    
    protected static class Select extends SimpleQueryTemplate {
        
        private final DataQueryTemplateProvider provider;

        public Select(DataQueryTemplateProvider provider) {
            this.provider = provider;
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            return provider.autoPartSelect(key);
        }
    }
    
    protected static class Insert extends SimpleQueryTemplate {
        
        private final DataQueryTemplateProvider provider;

        public Insert(DataQueryTemplateProvider provider) {
            this.provider = provider;
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            return provider.autoPartInsert(key);
        }
    }
    
    protected static class Update extends SimpleQueryTemplate {
        
        private final DataQueryTemplateProvider provider;

        public Update(DataQueryTemplateProvider provider) {
            this.provider = provider;
        }
        
        @Override
        protected QueryTemplatePart autoPart(String key) {
            return provider.autoPartUpdate(key);
        }
    }
    
    protected static class Delete extends SimpleQueryTemplate {
        
        private final DataQueryTemplateProvider provider;

        public Delete(DataQueryTemplateProvider provider) {
            this.provider = provider;
        }
        
        @Override
        protected QueryTemplatePart autoPart(String key) {
            return provider.autoPartDelete(key);
        }
    }
}
