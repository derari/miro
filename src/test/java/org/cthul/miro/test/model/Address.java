package org.cthul.miro.test.model;

import org.cthul.miro.dml.MappedDataQueryTemplateProvider;
import java.util.*;
import org.cthul.miro.map.*;
import org.cthul.miro.view.Views;

public class Address {
    
    int id = -1;
    String street = null;
    String city = null;
    List<Person1> inhabitants = new ArrayList<>();

    public Address() {
    }

    public Address(int id) {
        this.id = id;
    }

    public Address(String street, String city) {
        this.street = street;
        this.city = city;
    }
    
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

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.id;
        hash = 23 * hash + Objects.hashCode(this.street);
        hash = 23 * hash + Objects.hashCode(this.city);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Address other = (Address) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.street, other.street)) {
            return false;
        }
        if (!Objects.equals(this.city, other.city)) {
            return false;
        }
        return true;
    }
    
    private static final Mapping<Address> MAPPING = new ReflectiveMapping<>(Address.class);
    
    private static final MappedTemplateProvider<Address> TEMPLATE = new MappedDataQueryTemplateProvider<Address>(MAPPING) {{
        generatedKeys("a.id");
        attributes("a.street, a.city");
        table("Addresses a");
    }};
    
    public static final MappedView<Address> VIEW = Views.newView(MappedView.class, TEMPLATE);
//    public static final View<Query> VIEW = new QueryFactoryView<>(Query.class);
//    
//    // test-public
//    public static final SimpleMapping<Address> MAPPING = new ReflectiveMapping<>(Address.class);
//    
//    // test-public
//    public static final MappedQueryTemplate<Address> TEMPLATE = new MappedQueryTemplate<Address>() {{
//        select("id", "street", "city");
//        from("Addresses");
//        where("city_EQ", "city = ?");
//    }};
//    
//    public static class Query extends MappedTemplateQuery<Address> {
//
//        public Query(MiConnection cnn, String[] fields) {
//            super(cnn, MAPPING, TEMPLATE);
//            select(fields);
//        }
//
//        @Override
//        protected String queryString() {
//            String s = super.queryString();
//            System.out.println(s);
//            return s;
//        }
//        
//        public Query where() {
//            return this;
//        }
//        
//        public Query inCity(String city) {
//            where("city_EQ", city);
//            return this;
//        }
//    }
//    
//    public static final View<GQuery> GRAPH_VIEW = new QueryFactoryView<>(GQuery.class);
//    
//    private static final ZGraphQueryTemplate<Address> GRAPH_TEMPLATE = new ZGraphQueryTemplate<Address>() {{
//        key("id");
//        select("id", "street", "city");
//        from("Addresses");
//    }};
//    
//    public static class GQuery extends GraphQuery<Address> {
//        public GQuery(MiConnection cnn, String[] fields, View<GQuery> view) {
//            super(cnn, MAPPING, GRAPH_TEMPLATE, view);
//            select(fields);
//        }
//        @Override
//        protected String queryString() {
//            String s = super.queryString();
//            System.out.println(s);
//            return s;
//        }
//    }    
}
