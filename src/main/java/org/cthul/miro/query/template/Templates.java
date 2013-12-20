package org.cthul.miro.query.template;

import org.cthul.miro.query.QueryPartType;
import org.cthul.miro.query.OtherQueryPart;
import org.cthul.miro.query.InternalQueryBuilder;
import java.util.*;
import org.cthul.miro.query.parts.*;

public abstract class Templates {
    
    public static abstract class AbstractTemplatePart implements QueryTemplatePart {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
    
    public static abstract class StatelessQueryPart extends AbstractQueryPart implements QueryTemplatePart {
        
        private final Object[] required;
        private final QueryPartType partType;
        
        public StatelessQueryPart(Object key) {
            this(key, (Object[]) null);
        }
        
        public StatelessQueryPart(Object key, Object... required) {
            this(OtherQueryPart.VIRTUAL, key, required);
        }

        public StatelessQueryPart(QueryPartType partType, Object key, Object... required) {
            super(key);
            this.required = required;
            this.partType = partType;
        }

        public QueryPartType getPartType() {
            return partType;
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, required);
            queryBuilder.addPart(getPartType(), this);
            return this;
        }
    }
    
    public static abstract class ConfigurationTemplate extends AbstractTemplatePart {
        private final Object[] required;

        public ConfigurationTemplate() {
            this.required = null;
        }

        public ConfigurationTemplate(Object... required) {
            this.required = required;
        }
        
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, required);
            QueryPart p = new ConfigurationPart(this, queryBuilder, key);
            queryBuilder.addPart(OtherQueryPart.VIRTUAL, p);
            return p;
        }
        
        protected Object subKey(Object partKey, Object key) {
            return new CombinedKey(partKey, key);
        }
        
        protected boolean put(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... args) {
            if (key == null && args != null) {
                for (Object a: args) {
                    if (!put(queryBuilder, partKey, a, NO_ARGS)) {
                        return false;
                    }
                }
                return true;
            }
            Object subKey = subKey(partKey, key);
            if (queryBuilder.hasPart(subKey)) {
                if (args != null && args.length > 0) {
                    queryBuilder.put(subKey, args);
                }
                return true;
            }
            return putWithKey(queryBuilder, subKey, key, args);
        }
        
        protected boolean putWithKey(InternalQueryBuilder queryBuilder, Object newKey, Object key, Object... args) {
            return false;
        }
    }
    
    public static class ConfigurationPart extends AbstractQueryPart {

        private final ConfigurationTemplate template;
        private final InternalQueryBuilder queryBuilder;

        public ConfigurationPart(ConfigurationTemplate template, InternalQueryBuilder queryBuilder, Object key) {
            super(key);
            this.template = template;
            this.queryBuilder = queryBuilder;
        }

        @Override
        public void put(Object key, Object... args) {
//            if (key == null && args != null) {
//                for (Object a: args) {
//                    if (!template.put(queryBuilder, getKey(), a, NO_ARGS)) {
//                        super.putWithKey(a, NO_ARGS);
//                    }
//                }
//                return;
//            } else {
                if (template.put(queryBuilder, getKey(), key, args)) {
                    return;
                }
//            }
            super.put(key, args);
        }
    }
    
    public static class RequireAllTemplate extends ConfigurationTemplate {
        
        private final List<Object> keys  = new ArrayList<>();

        public RequireAllTemplate(Object key) {
            if (key != null) keys.add(key);
        }
        
        public RequireAllTemplate(Object... keys) {
            this.keys.addAll(Arrays.asList(keys));
        }
        
        public RequireAllTemplate(List<?> keys) {
            this.keys.addAll(keys);
        }
        
        public RequireAllTemplate(Object oneMore, List<?>... keys) {
            this(oneMore);
            for (List<?> k: keys) {
                this.keys.addAll(k);
            }
        }
        
        public RequireAllTemplate(List<?>... keys) {
            this(null, keys);
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, keys);
            return super.addPart(key, queryBuilder);
        }

        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... args) {
            return true;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + keyList(keys);
        }
    }
    
    public static class PutAllTemplate extends AbstractTemplatePart {
        
        private final List<Object> keys  = new ArrayList<>();
        private List<QueryTemplatePart> moreParts = null;

        public PutAllTemplate(Object key) {
            if (key != null) keys.add(key);
        }
        
        public PutAllTemplate(Object... keys) {
            this.keys.addAll(Arrays.asList(keys));
        }
        
        public PutAllTemplate(List<?> keys) {
            this.keys.addAll(keys);
        }
        
        public PutAllTemplate(Object oneMore, List<?>... keys) {
            this(oneMore);
            for (List<?> k: keys) {
                this.keys.addAll(k);
            }
        }
        
        public PutAllTemplate(List<?>... keys) {
            this(null, keys);
        }
        
        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            List<Object> allKeys = keys;
            requireAll(queryBuilder, keys);
            if (moreParts != null) {
                allKeys = new ArrayList<>(allKeys);
                for (QueryTemplatePart p: moreParts) {
                    QueryPart qp = p.addPart(key, queryBuilder);
                    allKeys.add(qp.getKey());
                }
            }
            return proxyPart(key, queryBuilder, allKeys);
        }
        
        public PutAllTemplate and(QueryTemplatePart... moreParts) {
            if (this.moreParts == null) this.moreParts = new ArrayList<>();
            this.moreParts.addAll(Arrays.asList(moreParts));
            return this;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + keyList(keys);
        }
    }
    
    public static class PutValuesTemplate extends AbstractTemplatePart {
        private final Object key;
        private final Object[] values;

        public PutValuesTemplate(Object key, Object... values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put(this.key, values);
            return virtualPart(key, queryBuilder);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + key + 
                    " -> " + keyList(values, ",");
        }
    }
    
    public static class Put2ValuesTemplate extends AbstractTemplatePart {
        private final Object key;
        private final Object key2;
        private final Object[] values;

        public Put2ValuesTemplate(Object key, Object key2, Object... values) {
            this.key = key;
            this.key2 = key2;
            this.values = values;
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            queryBuilder.put2(this.key, key2, values);
            return virtualPart(key, queryBuilder);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + key + "." + key2 +
                    " -> " + keyList(values, ",");
        }
    }
    
    public static class EachPut2Template extends AbstractTemplatePart {
        private final List<Object> keys = new ArrayList<>();
        private final Object key2;

        public EachPut2Template(Object key2, Object... keys) {
            this.key2 = key2;
            this.keys.addAll(Arrays.asList(keys));
        }

        public EachPut2Template(Object key2, List<Object>... keys) {
            this.key2 = key2;
            for (List<Object> l: keys) {
                this.keys.addAll(l);
            }
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (Object k: keys) {
                queryBuilder.put2(k, key2);
            }
            return virtualPart(key, queryBuilder);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + keyList(keys) + " ." + key2;
        }
    }
    
    public static class Put2AllTemplate extends AbstractTemplatePart {
        private final List<Object> key2s = new ArrayList<>();
        private final Object key;

        public Put2AllTemplate(Object key, Object... key2s) {
            this.key = key;
            this.key2s.addAll(Arrays.asList(key2s));
        }

        public Put2AllTemplate(Object key, List<?> key2s) {
            this.key = key;
            this.key2s.addAll(key2s);
        }

        public Put2AllTemplate(Object key, List<?>... key2s) {
            this.key = key;
            for (List<?> l: key2s) {
                this.key2s.addAll(l);
            }
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (Object k2: key2s) {
                queryBuilder.put2(this.key, k2);
            }
            return virtualPart(key, queryBuilder);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + key + ". " + keyList(key2s);
        }
    }
    
    public static class ProxyTemplate extends ConfigurationTemplate {
        private final List<Object> proxyKeys = new ArrayList<>();
        public ProxyTemplate(Object... keys) {
            this.proxyKeys.addAll(Arrays.asList(keys));
        }
        
        public ProxyTemplate(List<?>... keys) {
            for (List<?> l: keys) {
                this.proxyKeys.addAll(l);
            }
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (Object k: proxyKeys) {
                queryBuilder.put(k);
            }
            return super.addPart(key, queryBuilder);
        }
        
        @Override
        protected boolean put(InternalQueryBuilder queryBuilder, Object partKey, Object key, Object... args) {
            for (Object k: proxyKeys) {
                queryBuilder.put2(k, key, args);
            }
            return true;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " --> " + keyList(proxyKeys, ",");
        }
    }
    
    public static class AttributeDependenciesTemplate extends AbstractTemplatePart {
        
        private final List<Attribute> attributes;

        public AttributeDependenciesTemplate(Attribute... attributes) {
            this.attributes = Arrays.asList(attributes);
        }
        
        public AttributeDependenciesTemplate(List<Attribute>... attributes) {
            this.attributes = new ArrayList<>();
            for (List<Attribute> a: attributes) {
                this.attributes.addAll(a);
            }
        }

        @Override
        public QueryPart addPart(Object key, InternalQueryBuilder queryBuilder) {
            for (Attribute a: attributes) {
                a.addRequired(queryBuilder);
            }
            return virtualPart(key, queryBuilder);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + keyList(attributes, ",");
        }
    }
    
    public static class PlainAttributePart extends AbstractQueryPart implements AttributeQueryPart {
        
        protected final Attribute attribute;

        public PlainAttributePart(Object key, Attribute attribute) {
            super(key);
            this.attribute = attribute;
        }

        @Override
        public String getAttributeKey() {
            return attribute.getKey();
        }

        @Override
        public String getColumn() {
            return attribute.getColumnLiteral();
        }

        @Override
        public String getSelect() {
            return attribute.getSelect();
        }
        
        @Override
        public String toString() {
            return super.toString() + " " + attribute.getSelect();
        }
    }
    
    public static void requireAll(InternalQueryBuilder queryBuilder, Object... required) {
        if (required == null) return;
        for (Object s: required) {
            queryBuilder.put(s);
        }
    }
    
    public static void requireAll(InternalQueryBuilder queryBuilder, List<Object> required) {
        if (required == null) return;
        for (Object s: required) {
            queryBuilder.put(s);
        }
    }
    
    public static QueryPart virtualPart(Object key, InternalQueryBuilder queryBuilder) {
        QueryPart part = new VirtualQueryPart(key);
        queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
        return part;
    }
    
    public static QueryPart proxyPart(Object key, final InternalQueryBuilder queryBuilder, final List<Object> required) {
        QueryPart part = new VirtualQueryPart(key) {
            @Override
            public void put(Object key, Object... args) {
                for (Object r: required) {
                    queryBuilder.put2(r, key, args);
                }
            }
        };
        queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
        return part;
    }
    
    private static final Object[] NO_ARGS = {};
    
    public static String keyList(Collection<?> keys) {
        return keyList(keys.toArray());
    }
    
    public static String keyList(Collection<?> keys, String sep) {
        return keyList(keys.toArray(), sep);
    }
    
    public static String keyList(Object[] keys) {
        return keyList(keys, "+");
    }
    
    public static String keyList(Object[] keys, String sep) {
        if (keys == null) return "[]";
        StringBuilder sb = new StringBuilder();
        int len = Math.min(3, keys.length);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(sep);
            sb.append(keys[i]);
        }
        if (len < keys.length) {
            sb.append(sep);
            sb.append(keys.length-len);
            sb.append("...");
        }
        return sb.toString();
    }
}
