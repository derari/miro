package org.cthul.miro.test.model;

import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.QueryTemplate;
import org.cthul.miro.dsl.QueryWithTemplate;
import org.cthul.miro.dsl.View;
import org.cthul.miro.graph.GraphQuery;
import org.cthul.miro.graph.GraphQueryTemplate;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.util.QueryFactoryView;
import org.cthul.miro.util.ReflectiveMapping;

public class Address {
    
    int id = -1;
    String street = null;
    String city = null;

    public int getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }
    
    public static final View<Query> VIEW = new QueryFactoryView<>(Query.class);
    
    // test-public
    public static final Mapping<Address> MAPPING = new ReflectiveMapping<>(Address.class);
    
    // test-public
    public static final QueryTemplate<Address> TEMPLATE = new QueryTemplate<Address>() {{
        select("id", "street", "city");
        from("Addresses");
        where("city_EQ", "city = ?");
    }};
    
    public static class Query extends QueryWithTemplate<Address> {

        public Query(MiConnection cnn, String[] fields) {
            super(cnn, MAPPING, TEMPLATE);
            select_keys(fields);
        }

        @Override
        protected String queryString() {
            String s = super.queryString();
            System.out.println(s);
            return s;
        }
        
        public Query where() {
            return this;
        }
        
        public Query inCity(String city) {
            where_key("city_EQ", city);
            return this;
        }
    }
    
    public static final View<GQuery> GRAPH_VIEW = new QueryFactoryView<>(GQuery.class);
    
    private static final GraphQueryTemplate<Address> GRAPH_TEMPLATE = new GraphQueryTemplate<Address>() {{
        key("id");
        select("id", "street", "city");
        from("Addresses");
    }};
    
    public static class GQuery extends GraphQuery<Address> {
        public GQuery(MiConnection cnn, String[] fields, View<GQuery> view) {
            super(cnn, MAPPING, GRAPH_TEMPLATE, view);
            select_keys(fields);
        }
        @Override
        protected String queryString() {
            String s = super.queryString();
            System.out.println(s);
            return s;
        }
    }
}
