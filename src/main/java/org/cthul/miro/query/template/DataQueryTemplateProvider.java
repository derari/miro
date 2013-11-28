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
    
    protected QueryTemplatePart autoPartSelect(String key) {
        switch (key) {
            case "all-keys":
                return new SelectAllTemplate(
                        generatedKeys, naturalKeys);
            case "*":
                return new SelectAllTemplate("all-keys", 
                        defaultAttributes, defaultSelect);
            case "**":
                return new SelectAllTemplate("*", 
                        optionalAttributes, optionalSelect);
        }
        if (key.startsWith("groupBy-")) {
            Attribute at = attributes.get(key.substring(8));
            if (at != null) {
                return new GroupByTemplate(at);
            }
        }
        return autoPartCached(key);
    }
    
    protected QueryTemplatePart autoPartInsert(String key) {
        switch (key) {
            case "all-keys":
                return new SelectAllTemplate(naturalKeys);
            case "*":
                return new SelectAllTemplate("all-keys", defaultAttributes);
            case "**":
                return new SelectAllTemplate("*", optionalAttributes);
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
                return new SelectAllTemplate(defaultAttributes);
            case "**":
                return new SelectAllTemplate("*", optionalAttributes);
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
                return new SelectAllTemplate(
                        generatedKeys, naturalKeys);
            case "*":
            case "**":
                return new SelectAllTemplate("all-keys");
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
    
    protected List<Attribute> newAttributes(String sql) {
        String[][] attributeParts = SqlUtils.parseAttributes(sql);
        List<Attribute> result = new ArrayList<>(attributeParts.length);
        for (String[] p: attributeParts) {
            if (p[2] == null) p[2] = "main-table";
            Attribute a = new Attribute(p[0], p[1], p[2], p[3], p[4]);
            result.add(a);
            attributes.put(a.getKey(), a);
        }
        return result;
    }
    
    protected List<Attribute> newSelects(String sql) {
        String[][] attributeParts = SqlUtils.parseSelectClause(sql);
        List<Attribute> result = new ArrayList<>(attributeParts.length);
        for (String[] p: attributeParts) {
            Attribute a = new Attribute(p[0], p[1], p[2], p[3], null);
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
    
    protected void generatedKeys(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(a);
            addAttributeKeysTo(list, generatedKeys);
        }
    }
    
    protected void naturalKeys(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(a);
            addAttributeKeysTo(list, naturalKeys);
        }
    }
    
    protected void attributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(a);
            addAttributeKeysTo(list, defaultAttributes);
        }
    }
    
    protected void optionalAttributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(a);
            addAttributeKeysTo(list, optionalAttributes);
        }
    }
    
    protected void select(@MultiValue @AutoKey @AutoDependencies String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(s);
            addAttributeKeysTo(list, defaultSelect);
        }
    }
    
    protected void optionalSelect(@MultiValue @AutoKey @AutoDependencies String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(s);
            addAttributeKeysTo(list, optionalSelect);
        }
    }
    
    protected void table(@AutoKey String table) {
        String[] p = SqlUtils.parseFromPart(table);
        TableTemplate tt = new TableTemplate(p[0], p[1], p[2]);
        addIfNew(tt.getKey(), tt);
        addIfNew("main-table", tt);
    }
    
    protected void join(@AutoKey String join) {
        String[] p = SqlUtils.parseJoinPart(join);
        join(p[0], p[1]);
    }
    
    protected void join(String key, String join) {
        JoinTemplate jt = new JoinTemplate(join);
        add(key, jt);
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
    
    protected class SelectAllTemplate implements QueryTemplatePart {
        
        private final List<String> keys;

        public SelectAllTemplate(String key) {
            this(key, (List<String>[]) null);
        }
        
        public SelectAllTemplate(String... keys) {
            this(Arrays.asList(keys));
        }
        
        public SelectAllTemplate(String oneMore, List<String>... keys) {
            this.keys = new ArrayList<>();
            if (oneMore != null) this.keys.add(oneMore);
            for (List<String> k: keys) {
                this.keys.addAll(k);
            }
        }
        
        public SelectAllTemplate(List<String>... keys) {
            this(null, keys);
        }
        
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, keys);
            return virtualPart(key, queryBuilder);
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
            QueryPart part = new SimpleQueryPart(key, sql);
            queryBuilder.addPart(DataQueryPart.TABLE, part);
            return part;
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
            queryBuilder.require("filter-by-keys");
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
    
    protected static class JoinTemplate extends SimpleTemplatePart {

        public JoinTemplate(String sql, String... required) {
            super(DataQueryPart.JOIN, sql, required);
        }
    }
    
    protected class KeysInTemplate implements QueryTemplatePart {
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            queryBuilder.require("all-keys");
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

        public GroupByTemplate(Attribute at) {
            super(DataQueryPart.GROUP_BY, at.getKeyLiteral(), at.getKey());
        }
    }
    
    protected static final String[] AUTO_DEPENDENCIES = null;
    protected static final String[] NO_DEPENDENCIES = {};
    
    protected static class Using<This extends Using<? extends This>> {
        
        private final DataQueryTemplateProvider template;
        private final String[] required;

        public Using(DataQueryTemplateProvider template, String... required) {
            this.template = template;
            this.required = required;
        }
        
        protected This self() {
            return (This) this;
        }
        
        public This select(@MultiValue @AutoKey String... select) {
            template.select(select);
            return self();
        }
    }
    
    protected static void requireAll(InternalQueryBuilder queryBuilder, String... required) {
        for (String s: required) {
            queryBuilder.require(s);
        }
    }
    
    protected static void requireAll(InternalQueryBuilder queryBuilder, List<String> required) {
        for (String s: required) {
            queryBuilder.require(s);
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
