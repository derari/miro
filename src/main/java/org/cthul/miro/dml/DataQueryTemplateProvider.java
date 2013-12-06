package org.cthul.miro.dml;

import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.parts.SimpleQueryPart;
import java.util.*;
import org.cthul.miro.doc.AutoDependencies;
import org.cthul.miro.doc.AutoKey;
import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.query.parts.QueryPart;
import org.cthul.miro.query.sql.DataQuery;
import org.cthul.miro.query.sql.DataQueryPart;
import org.cthul.miro.query.template.Attribute;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.query.template.QueryTemplatePart;
import org.cthul.miro.query.template.QueryTemplateProvider;
import org.cthul.miro.query.template.AbstractQueryTemplate;
import org.cthul.miro.query.template.SimpleTemplatePart;
import org.cthul.miro.query.template.Templates;
import org.cthul.miro.query.template.UniqueKey;
import org.cthul.miro.util.SqlUtils;
import static org.cthul.miro.query.sql.DataQuery.Type.*;

public class DataQueryTemplateProvider implements QueryTemplateProvider {
    
    private final CustomTemplateParts parts = new CustomTemplateParts();
    private CustomTemplateParts selectParts = null;
    private CustomTemplateParts insertParts = null;
    private CustomTemplateParts updateParts = null;
    private CustomTemplateParts deleteParts = null;
    private final Map<String, Attribute> attributes = new HashMap<>();
    private final List<String> generatedKeys = new ArrayList<>();
    private final List<String> naturalKeys = new ArrayList<>();
    private final List<String> defaultAttributes = new ArrayList<>();
    private final List<String> optionalAttributes = new ArrayList<>();
    private final List<String> defaultSelect = new ArrayList<>();
    private final List<String> optionalSelect = new ArrayList<>();
    private final LinkedHashSet<String> internalSelect = new LinkedHashSet<>();
    
    private CustomTemplateParts currentParts = null;

    private QueryTemplate parent = null;
    private QueryTemplate select = null;
    private QueryTemplate insert = null;
    private QueryTemplate update = null;
    private QueryTemplate delete = null;
    
    public DataQueryTemplateProvider() {
        currentParts = parts;
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
    
    private QueryTemplate buildSelect() {
        return build(selectParts, parts, newSelect(getParent()));
    }
    
    private QueryTemplate buildInsert() {
        return build(insertParts, parts, newInsert(getParent()));
    }
    
    private QueryTemplate buildUpdate() {
        return build(updateParts, parts, newUpdate(getParent()));
    }
    
    private QueryTemplate buildDelete() {
        return build(deleteParts, parts, newDelete(getParent()));
    }
    
    private QueryTemplate build(CustomTemplateParts parts1, CustomTemplateParts parts2, QueryTemplate template) {
        if (parts2 != null) {
            template = parts2.newTemplate(template);
        }
        if (parts1 != null) {
            template = parts1.newTemplate(template);
        }
        return customize(template);
    }

    protected QueryTemplate customize(QueryTemplate template) {
        return template;
    }

    @Override
    public QueryTemplate getTemplate(QueryType<?> queryType) {
        currentParts = null;
        switch (DataQuery.Type.get(queryType)) {
            case SELECT:
                if (select == null) select = buildSelect();
                return select;
            case INSERT:
                if (insert == null) insert = buildInsert();
                return insert;
            case UPDATE:
                if (update == null) update = buildUpdate();
                return update;
            case DELETE:
                if (delete == null) delete = buildDelete();
                return delete;
        }
        return null;
    }
    
    private void checkModification() {
        if (currentParts == null) {
            throw new IllegalStateException("Modifications not allowed");
        }
    }
    
    protected synchronized void add(IncludeMode mode, Object key, QueryTemplatePart part) {
        checkModification();
        currentParts.add(mode, key, part);
    }
    
    protected synchronized void addIfNew(IncludeMode mode, Object key, QueryTemplatePart part) {
        checkModification();
        currentParts.addIfNew(mode, key, part);
    }
    
    protected void addAs(IncludeMode mode, QueryTemplatePart part, Object... keys) {
        for (Object k: keys) {
            add(mode, k, part);
        }
    }
    
    protected void configureMode(CustomTemplateParts parts) {
        checkModification();
        currentParts = parts;
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
    
    public LinkedHashSet<String> getInternalSelect() {
        return internalSelect;
    }
    
    private List<Object> getParts(IncludeMode mode, CustomTemplateParts more) {
        List<Object> list = parts.getParts(mode, Collections.emptyList());
        if (more != null) {
            list = more.getParts(mode, list);
        }
        return list;
    }
    
    public List<Object> getSelectParts(IncludeMode mode) {
        return getParts(mode, selectParts);
    }
    
    public List<Object> getInsertParts(IncludeMode mode) {
        return getParts(mode, insertParts);
    }
    
    public List<Object> getUpdateParts(IncludeMode mode) {
        return getParts(mode, updateParts);
    }
    
    public List<Object> getDeleteParts(IncludeMode mode) {
        return getParts(mode, deleteParts);
    }
    
    protected CustomTemplateParts getSelectOnlyParts() {
        if (selectParts == null) selectParts = new CustomTemplateParts();
        return selectParts;
    }
    
    protected CustomTemplateParts getInsertOnlyParts() {
        if (insertParts == null) insertParts = new CustomTemplateParts();
        return insertParts;
    }
    
    protected CustomTemplateParts getUpdateOnlyParts() {
        if (updateParts == null) updateParts = new CustomTemplateParts();
        return updateParts;
    }
    
    protected CustomTemplateParts getDeleteOnlyParts() {
        if (deleteParts == null) deleteParts = new CustomTemplateParts();
        return deleteParts;
    }
    
    protected Object newTemplatePartKey(String hint) {
        return new UniqueKey(hint);
    }
    
    protected Object[] getRequired(Object[] required, String[] part) {
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
    
    protected List<Attribute> newAttributes(Object[] required, String sql) {
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
    
    protected List<Attribute> newSelects(Object[] required, String sql) {
        String[][] attributeParts = SqlUtils.parseSelectClause(sql);
        List<Attribute> result = new ArrayList<>(attributeParts.length);
        for (String[] p: attributeParts) {
            required = getRequired(required, p);
            Attribute a = new Attribute(p[0], p[1], p[4], p[5], required);
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
    
    protected void generatedKeys(Object[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, generatedKeys);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void naturalKeys(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        naturalKeys(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void naturalKeys(Object[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, naturalKeys);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void attributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        attributes(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void attributes(Object[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, defaultAttributes);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void optionalAttributes(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        optionalAttributes(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void optionalAttributes(Object[] required, @MultiValue @AutoKey String... attributes) {
        for (String a: attributes) {
            List<Attribute> list = newAttributes(required, a);
            addAttributeKeysTo(list, optionalAttributes);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void select(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        select(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void select(Object[] required, @MultiValue @AutoKey String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(required, s);
            addAttributeKeysTo(list, defaultSelect);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void optionalSelect(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        optionalSelect(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void optionalSelect(Object[] required, @MultiValue @AutoKey String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(required, s);
            addAttributeKeysTo(list, optionalSelect);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void internalSelect(@MultiValue @AutoKey @AutoDependencies String... attributes) {
        optionalSelect(AUTO_DEPENDENCIES, attributes);
    }
    
    protected void internalSelect(Object[] required, @MultiValue @AutoKey String... selects) {
        for (String s: selects) {
            List<Attribute> list = newSelects(required, s);
            addAttributeKeysTo(list, internalSelect);
            addAttributesTo(list, this.attributes);
        }
    }
    
    protected void table(@AutoKey String table) {
        String[] p = SqlUtils.parseFromPart(table);
        TableTemplate tt = new TableTemplate(p[0], p[1], p[2]);
        addIfNew(IncludeMode.EXPLICIT, tt.getKey(), tt);
        addIfNew(IncludeMode.EXPLICIT, "main-table", new Templates.PutAllTemplate(tt.getKey()));
        // TODO: find out how to handle multiple tables and/or tables with no key
    }
    
    protected void join(@AutoKey String join) {
        join(NO_DEPENDENCIES, join);
    }
    
    protected void join(Object[] required, @AutoKey String join) {
        join(IncludeMode.EXPLICIT, required, join);
    }
    
    protected void join(String key, String join) {
        join(IncludeMode.EXPLICIT, key, join);
    }
    
    protected void join(Object[] required, String key, String join) {
        join(IncludeMode.EXPLICIT, required, key, join);
    }
    
    protected void join(IncludeMode mode, Object[] required, @AutoKey String join) {
        String[] p = SqlUtils.parseJoinPart(join);
        join(mode, required, p[0], p[1]);
    }
    
    protected void join(IncludeMode mode, String key, String join) {
        join(mode, NO_DEPENDENCIES, key, join);
    }
    
    protected void join(IncludeMode mode, Object[] required, String key, String join) {
        JoinTemplate jt = new JoinTemplate(join, required);
        add(mode, key, jt);
    }
    
    protected void virtual(Object[] required, Object key) {
        virtual(IncludeMode.EXPLICIT, required, key);
    }
    
    protected void virtual(IncludeMode mode, Object[] required, Object key) {
        QueryTemplatePart vt = new Templates.PutAllTemplate(required);
        add(mode, key, vt);
    }
    
    protected void where(Object[] required, Object key, String sql) {
        where(IncludeMode.EXPLICIT, required, key, sql);
    }
    
    protected void where(IncludeMode mode, Object[] required, Object key, String sql) {
        if (required == AUTO_DEPENDENCIES) {
            throw new IllegalArgumentException(
                    "Autodetect not supported.");
        }
        QueryTemplatePart wt = new SimpleTemplatePart(DataQueryPart.WHERE, sql, required);
        add(mode, key, wt);
    }
    
    protected void groupBy(@AutoKey String sql) {
        groupBy(IncludeMode.EXPLICIT, sql);
    }
    
    protected void groupBy(Object key, String sql) {
        groupBy(IncludeMode.EXPLICIT, key, sql);
    }
    
    protected void groupBy(Object[] required, Object key, String sql) {
        groupBy(IncludeMode.EXPLICIT, required, key, sql);
    }
    
    protected void groupBy(IncludeMode mode, @AutoKey String sql) {
        String[] part = SqlUtils.parseGroupPart(sql);
        String key = "groupBy-" + part[0];
        groupBy(mode, key, sql);
    }
    
    protected void groupBy(IncludeMode mode, Object key, String sql) {
        groupBy(mode, NO_DEPENDENCIES, key, sql);
    }
    
    protected void groupBy(IncludeMode mode, Object[] required, Object key, String sql) {
        if (required == AUTO_DEPENDENCIES) {
            throw new IllegalArgumentException(
                    "Autodetect not supported.");
        }
        QueryTemplatePart wt = new SimpleTemplatePart(DataQueryPart.GROUP_BY, sql, required);
        getSelectOnlyParts().add(mode, key, wt);
    }
    
    protected void orderBy(@AutoKey String sql) {
        orderBy(IncludeMode.EXPLICIT, sql);
    }
    
    protected void orderBy(Object key, String sql) {
        orderBy(IncludeMode.EXPLICIT, key, sql);
    }
    
    protected void orderBy(Object[] required, Object key, String sql) {
        orderBy(IncludeMode.EXPLICIT, required, key, sql);
    }
    
    protected void orderBy(IncludeMode mode, @AutoKey @AutoDependencies String sql) {
        String[] part = SqlUtils.parseOrderPart(sql);
        String key = "orderBy-" + part[0];
        orderBy(mode, new Object[]{part[2]}, key, sql);
    }
    
    protected void orderBy(IncludeMode mode, Object key, @AutoDependencies String sql) {
        orderBy(mode, AUTO_DEPENDENCIES, key, sql);
    }
    
    protected void orderBy(IncludeMode mode, Object[] required, Object key, @AutoDependencies String sql) {
        if (required == AUTO_DEPENDENCIES) {
            String[] part = SqlUtils.parseOrderPart(sql);
            required = new Object[]{part[2]};
        }
        QueryTemplatePart wt = new SimpleTemplatePart(DataQueryPart.ORDER_BY, sql, required);
        getSelectOnlyParts().add(mode, key, wt);
    }
    
    protected Using<?> using(IncludeMode include, CustomTemplateParts parts, Object... required) {
        return new Using<>(this, include, parts, required);
    }
    
    protected Using<?> using(Object... required) {
        return using(IncludeMode.EXPLICIT, null, required);
    }
    
    protected Object[] dependenciesForUsing(Object[] required) {
        if (required == AUTO_DEPENDENCIES || required == NO_DEPENDENCIES) {
            return required;
        }
        String key;
        if (required != null && required.length > 0) {
            StringBuilder sb = new StringBuilder();
            int l = Math.min(3, required.length);
            for (int i = 0; i < l; i++) {
                if (sb.length() > 0) sb.append('+');
                sb.append(required[i]);
            }
            if (required.length > 3) sb.append("+").append(required.length-3);
            key = sb.toString();
        } else {
            key = "virtual";
        }
        Object oKey = newTemplatePartKey(key);
        virtual(required, oKey);
        return new Object[]{oKey};
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
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
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

        public TableQueryPart(Object key, String sql) {
            super(key, sql);
        }

        @Override
        public void put(Object key, Object... args) {
            if (args != null && args.length > 0) {
                super.put(key, args);
            }
        }
    }
    
    protected static class JoinTemplate implements QueryTemplatePart {

        private final String sql;
        private final Object[] required;

        public JoinTemplate(String sql, Object... required) {
            this.sql = sql;
            this.required = required;
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            Templates.requireAll(queryBuilder, required);
            TableQueryPart jp = new TableQueryPart(key, sql);
            queryBuilder.addPart(DataQueryPart.JOIN, jp);
            return jp;
        }
    }
    
    protected static final Object[] AUTO_DEPENDENCIES = null;
    protected static final Object[] NO_DEPENDENCIES = {};
    protected static final Object[] ALWAYS = {1};
    
    protected static class Using<This extends Using<? extends This>> {
        
        private final DataQueryTemplateProvider template;
        private final Object[] required;
        private final CustomTemplateParts originalParts;
        private final CustomTemplateParts actualParts;
        private final IncludeMode include;

        public Using(DataQueryTemplateProvider template, IncludeMode include, CustomTemplateParts actualParts, Object... required) {
            this.template = template;
            this.originalParts = template.currentParts;
            this.actualParts = actualParts;
            this.include = include;
            this.required = template.dependenciesForUsing(required);
        }
        
        protected DataQueryTemplateProvider templateWithMode() {
            if (actualParts != null) {
                template.configureMode(actualParts);
            }
            return template;
        }
        
        protected This restoreMode() {
            if (actualParts != null) {
                template.configureMode(originalParts);
            }
            return (This) this;
        }
        
        public This using(Object... keys) {
            return (This) template.using(include, actualParts, keys);
        }
        
        public This select(@MultiValue @AutoKey String... select) {
            templateWithMode().select(required, select);
            return restoreMode();
        }
        
        public This optionalSelect(@MultiValue @AutoKey String... select) {
            templateWithMode().optionalSelect(required, select);
            return restoreMode();
        }
        
        public This internalSelect(@MultiValue @AutoKey String... select) {
            templateWithMode().optionalSelect(required, select);
            return restoreMode();
        }
        
        public This join(@AutoKey String join) {
            templateWithMode().join(include, required, join);
            return restoreMode();
        }
    }
    
    protected static class CustomTemplateParts {
        
        private final Map<Object, QueryTemplatePart> parts = new HashMap<>();
        private List<Object> always = null;
        private List<Object> byDefault = null;
        private List<Object> optional = null;
        
        public QueryTemplate newTemplate(QueryTemplate parent) {
            return new CustomTemplate(parts, parent);
        }

        public void addIfNew(IncludeMode mode, Object key, QueryTemplatePart part) {
            if (parts.containsKey(key)) return;
            add(mode, key,part);
        }

        public void add(IncludeMode mode, Object key, QueryTemplatePart part) {
            parts.put(key,part);
            switch (mode) {
                case ALWAYS:
                    if (always == null) always = new ArrayList<>();
                    always.add(key);
                case DEFAULT:
                    if (byDefault == null) byDefault = new ArrayList<>();
                    byDefault.add(key);
                case OPTIONAL:
                    if (optional == null) optional = new ArrayList<>();
                    optional.add(key);
            }
        }
        
        private List<Object> getParts(List<Object> myList, List<Object> list) {
            if (myList == null) return list;
            List<Object> result = new ArrayList<>(list);
            result.addAll(myList);
            return result;
        }

        public List<Object> getParts(IncludeMode mode, List<Object> list) {
            switch (mode) {
                case ALWAYS:
                    return getParts(always, list);
                case DEFAULT:
                    return getParts(byDefault, list);
                case OPTIONAL:
                    return getParts(optional, list);
            }
            return list;
        }
    }
    
    protected static class CustomTemplate extends AbstractQueryTemplate {

        private final Map<Object, QueryTemplatePart> parts;

        public CustomTemplate(Map<Object, QueryTemplatePart> parts, QueryTemplate parent) {
            super(parent);
            this.parts = parts;
        }
        
        @Override
        protected QueryTemplatePart autoPart(Object key) {
            QueryTemplatePart part = parts.get(key);
            if (part != null) return part;
            return super.autoPart(key);
        }
    }
}
