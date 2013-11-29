package org.cthul.miro.test.model;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.QueryFactoryView;
import org.cthul.miro.dsl.View;
import org.cthul.miro.graph.GraphQuery;
import org.cthul.miro.graph.GraphQueryTemplate;
import org.cthul.miro.map.z.MappedQueryTemplate; 
import org.cthul.miro.map.z.MappedTemplateQuery;
import org.cthul.miro.map.z.SimpleMapping;
import org.cthul.miro.map.z.ReflectiveMapping;

public class Address {
    
    int id = -1;
    String street = null;
    String city = null;
    List<Person1> inhabitants = new ArrayList<>();

    public int getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public List<Person1> getInhabitants() {
        return inhabitants;
    }
    
    public static final View<Query> VIEW = new QueryFactoryView<>(Query.class);
    
    // test-public
    public static final SimpleMapping<Address> MAPPING = new ReflectiveMapping<>(Address.class);
    
    // test-public
    public static final MappedQueryTemplate<Address> TEMPLATE = new MappedQueryTemplate<Address>() {{
        select("id", "street", "city");
        from("Addresses");
        where("city_EQ", "city = ?");
    }};
    
    public static class Query extends MappedTemplateQuery<Address> {

        public Query(MiConnection cnn, String[] fields) {
            super(cnn, MAPPING, TEMPLATE);
            select(fields);
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
            where("city_EQ", city);
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
            select(fields);
        }
        @Override
        protected String queryString() {
            String s = super.queryString();
            System.out.println(s);
            return s;
        }
    }    
}
