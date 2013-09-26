package org.cthul.miro.test.model;

import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.dsl.QueryFactoryView;
import org.cthul.miro.map.*;

public class Person0 {

    int id = -1;
    String firstName = null;
    String lastName = null;
    String street = null;
    String city = null;

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }
    
    public static View<Query> VIEW = new QueryFactoryView<>(Query.class);
    
    private static final Mapping<Person0> MAPPING = new ReflectiveMapping<>(Person0.class);
    
    private static final MappedTemplate<Person0> TEMPLATE = new MappedTemplate<Person0>() {{
        select("p.id", "firstName", "lastName",
               "a.street", "a.city"); // should autodetect 'a' required
        from("People p");
        join("Addresses a ON p.addressId = a.id");
        where("lastName_LIKE", "lastName LIKE ?");
        using("a")
            .where("city_EQ", "a.city = ?");
    }};
    
    public static class Query extends MappedTemplateQuery<Person0> {

        public Query(MiConnection cnn, String[] select) {
            super(cnn, MAPPING, TEMPLATE);
            query().select(select);
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
            query().where("city_EQ", city);
            return this;
        }
    }
}
