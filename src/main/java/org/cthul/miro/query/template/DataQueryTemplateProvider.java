package org.cthul.miro.query.template;

import org.cthul.miro.query.parts.SimpleQueryPart;
import java.util.*;
import org.cthul.miro.doc.AutoDependencies;
import org.cthul.miro.doc.AutoKey;
import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.util.SqlUtils;
import static org.cthul.miro.query.sql.DataQuery.Type.*;

public class DataQueryTemplateProvider implements QueryTemplateProvider {
    
    private QueryTemplate parent = null;
    private QueryTemplate select = null;
    private QueryTemplate insert = null;
    private QueryTemplate update = null;
    private QueryTemplate delete = null;
    
    private final Map<String, QueryTemplatePart> parts = new HashMap<>();
    private final Map<String, Attribute> attributes = new HashMap<>();
    private final List<String> generatedKeys = new ArrayList<>();
    private final List<String> naturalKeys = new ArrayList<>();
    private final List<String> defaultAttributes = new ArrayList<>();
    private final List<String> optionalAttributes = new ArrayList<>();
    private final List<String> defaultSelect = new ArrayList<>();
    private final List<String> optionalSelect = new ArrayList<>();

    public DataQueryTemplateProvider() {
    }
    
    protected QueryTemplate newParent(QueryTemplate parent) {
        return new DataQueryTemplate.Parent(this, parent);
    }
    
    protected QueryTemplate newSelect(QueryTemplate parent) {
        return new DataQueryTemplate.Select(this, parent);
    }

    protected QueryTemplate newInsert(QueryTemplate parent) {
        return new DataQueryTemplate.Insert(this, parent);
    }

    protected QueryTemplate newUpdate(QueryTemplate parent) {
        return new DataQueryTemplate.Update(this, parent);
    }

    protected QueryTemplate newDelete(QueryTemplate parent) {
        return new DataQueryTemplate.Delete(this, parent);
    }
    
    protected QueryTemplate getParent() {
        if (parent == null) return newParent(null);
        return parent;
    }
    
    protected QueryTemplate customize(QueryTemplate template) {
        return new CustomTemplateParts(template);
    }

    @Override
    public QueryTemplate getTemplate(QueryType<?> queryType) {
        switch (DataQuery.Type.get(queryType)) {
            case SELECT:
                if (select == null) select = customize(newSelect(getParent()));
                return select;
            case INSERT:
                if (insert == null) insert = customize(newInsert(getParent()));
                return insert;
            case UPDATE:
                if (update == null) update = customize(newUpdate(getParent()));
                return update;
            case DELETE:
                if (delete == null) delete = customize(newDelete(getParent()));
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

    public Attribute getAttribute(String key) {
        return attributes.get(key);
    }

    public List<String> getGeneratedKeys() {
        return generatedKeys;
    }

    public List<String> getNaturalKeys() {
        return naturalKeys;
    }

    public List<String> getKeys() {
        List<String> result = new ArrayList<>(getGeneratedKeys());
        result.addAll(getNaturalKeys());
        return result;
    }

    public List<String> getDefaultAttributes() {
        return defaultAttributes;
    }

    public List<String> getOptionalAttributes() {
        return optionalAttributes;
    }
    
    public List<Attribute> getKeyAttributes() {
        return getAttributes(generatedKeys, naturalKeys);
    }

    public List<String> getDefaultSelect() {
        return defaultSelect;
    }

    public List<String> getOptionalSelect() {
        return optionalSelect;
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
        addIfNew("main-table", new Templates.PutAllTemplate(tt.getKey()));
        // TODO: find out how to handle multiple tables and/or tables with no key
    }
    
    protected void join(@AutoKey String join) {
        join(NO_DEPENDENCIES, join);
    }
    
    protected void join(String[] required, @AutoKey String join) {
        String[] p = SqlUtils.parseJoinPart(join);
        join(required, p[0], p[1]);
    }
    
    protected void join(String key, String join) {
        join(NO_DEPENDENCIES, key, join);
    }
    
    protected void join(String[] required, String key, String join) {
        JoinTemplate jt = new JoinTemplate(join);
        add(key, jt);
    }
    
    protected void virtual(String[] required, String key) {
        QueryTemplatePart vt = new Templates.PutAllTemplate(required);
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
    
    protected static class JoinTemplate implements QueryTemplatePart {

        private final String sql;
        private final String[] required;

        public JoinTemplate(String sql, String... required) {
            this.sql = sql;
            this.required = required;
        }

        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            Templates.requireAll(queryBuilder, required);
            TableQueryPart jp = new TableQueryPart(key, sql);
            queryBuilder.addPart(DataQueryPart.JOIN, jp);
            return jp;
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
        
        public This join(@AutoKey String join) {
            template.join(join);
            return self();
        }
    }
    
    private class CustomTemplateParts extends SimpleQueryTemplate {

        public CustomTemplateParts(QueryTemplate parent) {
            super(parent);
        }

        @Override
        protected QueryTemplatePart autoPart(String key) {
            QueryTemplatePart part = parts.get(key);
            if (part != null) return part;
            return super.autoPart(key);
        }
    }
}
