package org.cthul.miro.test.model;

import org.cthul.miro.MiConnection;
import org.cthul.miro.at.Join;
import org.cthul.miro.at.More;
import org.cthul.miro.dsl.QueryFactoryView;
import org.cthul.miro.dsl.View;
import org.cthul.miro.graph.GraphQuery;
import org.cthul.miro.graph.GraphQueryTemplate;
import org.cthul.miro.graph.SelectByKey;
import org.cthul.miro.map.SimpleMapping;
import org.cthul.miro.map.ReflectiveMapping;

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
    
    private static final SimpleMapping<Person1> MAPPING = new ReflectiveMapping<>(Person1.class);
    
    private static final GraphQueryTemplate<Person1> TEMPLATE = new GraphQueryTemplate<Person1>() {{
        key("id");
        select("p.id", "firstName", "lastName");
        internal_select("addressId");
        from("People p");
        where("lastName_LIKE", "lastName LIKE ?");
        relation("address", Address.GRAPH_VIEW, "addressId");        
        
        join("Addresses a ON p.addressId = a.id");
        using("a")
                .where("city_EQ", "a.city = ?");
    }};

    public static class Query extends GraphQuery<Person1> {

        public Query(MiConnection cnn, String[] select, View<Query> view) {
            super(cnn, MAPPING, TEMPLATE, view);
            select(select);
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
            where("city_EQ", city);
            return this;
        }
    }
}
