package org.cthul.miro.test.model;

import org.cthul.miro.dml.MappedDataQueryTemplateProvider;
import org.cthul.miro.map.*;
import org.cthul.miro.view.Views;

public class Person0 {

    int id = -1;
    String firstName = null;
    String lastName = null;
    String street = null;
    String city = null;
    String someFlag = null;

    public Person0() {
    }
    
    public Person0(int id) {
        this.id = id;
    }
    
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

    public String getSomeFlag() {
        return someFlag;
    }
    
    private static final Mapping<Person0> MAPPING = new ReflectiveMapping<>(Person0.class);
    
    private static final MappedTemplateProvider<Person0> TEMPLATE = new MappedDataQueryTemplateProvider<Person0>(MAPPING) {{
        generatedKeys("p.id");
        attributes("first_name AS firstName, last_name AS lastName");
        select("a.street, a.city");
        table("People p");
        join("Addresses a ON p.address_id = a.id");
    }};
    
    public static final MappedView<Person0> VIEW = Views.newView(MappedView.class, TEMPLATE);
//    
//    
//    public static View<TQuery> VIEW = new QueryFactoryView<>(TQuery.class);
//    
//    private static final SimpleMapping<Person0> MAPPING = new ReflectiveMapping<>(Person0.class);
//    
//    private static final MappedQueryTemplate<Person0> TEMPLATE = new MappedQueryTemplate<Person0>() {{
//        select("p.id", "firstName", "lastName",
//               "a.street", "a.city"); // should autodetect 'a' require
//        from("People p");
//        join("Addresses a ON p.addressId = a.id");
//        where("lastName_LIKE", "lastName LIKE ?");
//        using("a")
//            .where("city_EQ", "a.city = ?");
//        using("firstName", "lastName")
//            .configure("fullName", new CfgFullName());
//    }};
//    
//    public static class TQuery extends MappedTemplateQuery<Person0> {
//
//        public TQuery(MiConnection cnn, String[] select) {
//            super(cnn, MAPPING, TEMPLATE);
//            query().select(select);
//        }
//        
//        public String getQueryString() {
//            return super.queryString();
//        }
//        
//        @Override
//        protected String queryString() {
//            String s = super.queryString();
//            System.out.println(s);
//            return s;
//        }
//        
//        public TQuery where() {
//            return this;
//        }
//        
//        public TQuery inCity(String city) {
//            query().where("city_EQ", city);
//            return this;
//        }
//    }
//    
//    private static class CfgFullName implements EntityConfiguration<Person0> {
//        @Override
//        public EntityInitializer<Person0> newInitializer(ResultSet rs) throws SQLException {
//            return new InitFullName(rs);
//        }
//    }
//    
//    private static class InitFullName extends EntityBuilderBase implements EntityInitializer<Person0> {
//        private final ResultSet rs;
//        private final int cFirstName, cLastName;
//
//        public InitFullName(ResultSet rs) throws SQLException {
//            this.rs = rs;
//            cFirstName = getFieldIndex(rs, "firstName");
//            cLastName = getFieldIndex(rs, "lastName");
//        }
//        @Override
//        public void apply(Person0 entity) throws SQLException {
//            String fn = rs.getString(cFirstName);
//            String ln = rs.getString(cLastName);
//            entity.lastName = fn + " " + ln;
//        }
//        @Override
//        public void complete() throws SQLException { }
//        @Override
//        public void close() throws SQLException { }
//    }
//    
//    public static final View<AtQuery> AT_VIEW = new AnnotatedView<>(AtQuery.class, MAPPING);
//    public static final View<AtQuery2> AT_VIEW2 = new AnnotatedView<>(AtQuery2.class, MAPPING);
//    
//    @MiQuery(
//    select = @Select("p.id, firstName, lastName, a.street, a.city"),
//    from = @From("People p"),
//    join = @Join("Addresses a ON p.addressId = a.id"),
//    more = @More(require="a",
//        where = @Where(key="atAddress",value="a.city = ? AND a.street = ?")),
//    orderBy = @OrderBy(key="asc_$1",value={"lastName", "firstName"}),
//    impl = @Impl(AtQueryImpl.class)
//    )
//    public static interface AtQuery extends 
//                    AnnotatedMappedStatement<Person0>,
//                    AnnotatedQueryBuilder {
//        
//        AtQuery with();
//        
//        @Config(impl=CfgSetField.class, args={@Arg(t="firstName"), @Arg(t="-redacted-")})
//        AtQuery firstNameRedacted();
//        
//        @Config(impl=CfgSetField.class, args={@Arg(t="firstName"), @Arg(key="0")})
//        AtQuery firstNameAs(String value);
//        
//        AtQuery where();
//        
//        @Where("p.id = ?")
//        AtQuery id(int i);
//        
//        @Where("lastName LIKE ?")
//        AtQuery lastNameLike(String name);
//        
//        @Require("a")
//        @Where("a.city = ?")
//        AtQuery inCity(String city);
//        
//        @Impl(value=AtQueryImpl.class, method="atAddress", args={@Arg(t="City2"),@Arg(key="0")})
//        AtQuery inCity2(String street);
//        
//        AtQuery atAddress(String city, String street);
//        
//        @Require({"asc_lastName", "asc_firstName"})
//        AtQuery orderByName();
//    }
//    
//    @Always(@More(
//        config = @Config(impl=InitSomeFlag.class, args=@Arg(x=true))
//    ))
//    public static interface AtQuery2 extends AtQuery {
//        
//    }
//    
//    public static String lastAddress = "";
//    
//    private static class AtQueryImpl {
//        
//        public static void atAddress(MappedTemplateQuery qry, String city, String street) {
//            qry.put("atAddress", city, street);
//            lastAddress = city + ", " + street;
//        }
//    }
//    
//    public static class InitSomeFlag implements EntityInitializer<Person0> {
//        private boolean flag;
//
//        public InitSomeFlag(boolean flag) {
//            this.flag = flag;
//        }
//
//        @Override
//        public void apply(Person0 entity) throws SQLException {
//            entity.someFlag = flag ? "true" : "false";
//        }
//
//        @Override
//        public void complete() throws SQLException { }
//
//        @Override
//        public void close() throws SQLException { }
//    }
}
