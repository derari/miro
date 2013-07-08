package org.cthul.miro.test.model;

import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.*;
import org.cthul.miro.graph.GraphQuery;
import org.cthul.miro.graph.GraphQueryTemplate;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.util.QueryFactoryView;
import org.cthul.miro.util.ReflectiveMapping;

/**
 *
 */
public class Person1 {

    int id = -1;
    String firstName = null;
    String lastName = null;
    Address address = null;

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Address getAddress() {
        return address;
    }
    
    public static View<Query> VIEW = new QueryFactoryView<>(Query.class);
    
    private static final Mapping<Person1> MAPPING = new ReflectiveMapping<>(Person1.class);
    
    private static final GraphQueryTemplate<Person1> TEMPLATE = new GraphQueryTemplate<Person1>() {{
        key("id");
        select("p.id", "firstName", "lastName");
        internal_select("addressId");
        from("People p");
        join("Addresses a ON p.addressId = a.id");
        where("lastName_LIKE", "lastName LIKE ?");
        using("a")
                .where("city_EQ", "a.city = ?");
        relation("address", Address.GRAPH_VIEW, "addressId");
    }};

    public static class Query extends GraphQuery<Person1> {

        public Query(MiConnection cnn, String[] select, View<Query> view) {
            super(cnn, MAPPING, TEMPLATE, view);
            select_keys(select);
        }

        public String getQueryString() {
            return super.queryString();
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
}
