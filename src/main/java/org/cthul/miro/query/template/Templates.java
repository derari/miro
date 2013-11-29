package org.cthul.miro.query.template;

import java.util.*;
import org.cthul.miro.query.api.*;
import org.cthul.miro.query.parts.*;

public abstract class Templates {
    
    public static class PutAllTemplate implements QueryTemplatePart {
        
        private final List<String> keys;

        public PutAllTemplate(String key) {
            this.keys = new ArrayList<>();
            if (key != null) keys.add(key);
        }
        
        public PutAllTemplate(String... keys) {
            this(Arrays.asList(keys));
        }
        
        public PutAllTemplate(String oneMore, List<String>... keys) {
            this(oneMore);
            for (List<String> k: keys) {
                this.keys.addAll(k);
            }
        }
        
        public PutAllTemplate(List<String>... keys) {
            this(null, keys);
        }
        
        @Override
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            requireAll(queryBuilder, keys);
            return proxyPart(key, queryBuilder, keys);
        }
    }
    
    public static class PutValuesTemplate implements QueryTemplatePart {
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
    
    public static class Put2ValuesTemplate implements QueryTemplatePart {
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
    
    public static class AttributeDependenciesTemplate implements QueryTemplatePart {
        
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
        public QueryPart addPart(String key, InternalQueryBuilder queryBuilder) {
            for (Attribute a: attributes) {
                a.addRequired(queryBuilder);
            }
            return virtualPart(key, queryBuilder);
        }
    }
    
    public static class PlainAttributePart extends AbstractQueryPart implements AttributeQueryPart {
        
        protected final Attribute attribute;

        public PlainAttributePart(String key, Attribute attribute) {
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
    
    public static void requireAll(InternalQueryBuilder queryBuilder, String... required) {
        for (String s: required) {
            queryBuilder.put(s);
        }
    }
    
    public static void requireAll(InternalQueryBuilder queryBuilder, List<String> required) {
        for (String s: required) {
            queryBuilder.put(s);
        }
    }
    
    public static QueryPart virtualPart(String key, InternalQueryBuilder queryBuilder) {
        QueryPart part = new VirtualQueryPart(key);
        queryBuilder.addPart(OtherQueryPart.VIRTUAL, part);
        return part;
    }
    
    public static QueryPart proxyPart(String key, final InternalQueryBuilder queryBuilder, final List<String> required) {
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
}
