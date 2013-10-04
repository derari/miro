package org.cthul.miro.test.model;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.at.*;
import org.cthul.miro.dsl.View;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.dsl.QueryFactoryView;
import org.cthul.miro.map.*;
import org.cthul.miro.query.QueryBuilder;
import org.cthul.miro.util.CfgSetField;
import org.cthul.objects.instance.Arg;

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
    
    public static View<TQuery> VIEW = new QueryFactoryView<>(TQuery.class);
    
    private static final Mapping<Person0> MAPPING = new ReflectiveMapping<>(Person0.class);
    
    private static final MappedQueryTemplate<Person0> TEMPLATE = new MappedQueryTemplate<Person0>() {{
        select("p.id", "firstName", "lastName",
               "a.street", "a.city"); // should autodetect 'a' require
        from("People p");
        join("Addresses a ON p.addressId = a.id");
        where("lastName_LIKE", "lastName LIKE ?");
        using("a")
            .where("city_EQ", "a.city = ?");
    }};
    
    public static class TQuery extends MappedTemplateQuery<Person0> {

        public TQuery(MiConnection cnn, String[] select) {
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
        
        public TQuery where() {
            return this;
        }
        
        public TQuery inCity(String city) {
            query().where("city_EQ", city);
            return this;
        }
    }
    
    public static final View<AtQuery> AT_VIEW = new AnnotatedView<>(AtQuery.class, MAPPING);
    
    @MiQuery(
            select = @Select("p.id, firstName, lastName, a.street, a.city"),
            from = "People p",
            join = @Join("Addresses a ON p.addressId = a.id"),
            more = @More(using="a",
                where = @Where(key="atAddress",value="a.city = ? AND a.street = ?")),
            orderBy = @OrderBy(key="asc_$1",value={"lastName", "firstName"})
            )
    public static interface AtQuery extends AnnotatedStatement<Person0> {
        
        AtQuery with();
        
        @Config(impl=CfgSetField.class, args={@Arg(t="firstName"), @Arg(t="-redacted-")})
        AtQuery firstNameRedacted();
        
        @Config(impl=CfgSetField.class, args={@Arg(t="firstName"), @Arg(key="0")})
        AtQuery firstNameAs(String value);
        
        AtQuery where();
        
        @Where("p.id = ?")
        AtQuery id(int i);
        
        @Where("lastName LIKE ?")
        AtQuery lastNameLike(String name);
        
        @Using("a")
        @Where("a.city = ?")
        AtQuery inCity(String city);
        
        @Impl(value=AtQueryImpl.class, method="atAddress", args={@Arg(t="City2"),@Arg(key="0")})
        AtQuery inCity2(String street);
        
        @Using({"asc_lastName", "asc_firstName"})
        AtQuery orderByName();
        
        // --- for testing ---
        String getQueryString();
        List<Object> getArguments();
    }
    
    public static String lastAddress = "";
    
    private static class AtQueryImpl {
        
        public static void atAddress(MappedTemplateQuery qry, String city, String street) {
            qry.put("atAddress", city, street);
            lastAddress = city + ", " + street;
        }
    }
}
