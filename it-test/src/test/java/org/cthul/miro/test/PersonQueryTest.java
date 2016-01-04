package org.cthul.miro.test;

import org.junit.BeforeClass;

public class PersonQueryTest {

    @BeforeClass
    public static void setUp() {
        TestDB.scenario1();
    }
    
//    @AfterClass
//    public static void tearDown() {
//        TestDB.clear();
//    }
//    
//    private final MiConnection connection = new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax());
//    private final SqlConnection sql = SqlConnection.wrap(connection);
//    
//    private static final Object[] FULL_NAME = {"firstName", "lastName"};
//    
//    private static final ViewR<SelectPeople> PEOPLE = attributes -> {
//                return null;
//            };
//    
//    public static final EntityType<Person> PERSON_TYPE = BasicEntityType
//                        .build(Person::new, Person[]::new);
////                        .with(AttributeMapping.<Person>build()
////                                .optional("id", (p,rs,i) -> p.id = rs.getInt(i))
////                                .optional("first_name", (p,rs,i) -> p.firstName = rs.getString(i))
////                                .optional("last_name", (p,rs,i) -> p.lastName = rs.getString(i)))
//
//    private static final MappedSqlTemplateBuilder<Person> TB_PEOPLE = new MappedSqlTemplateBuilder<Person>(Person.class){{
//        attribute("id", p -> p.id, (p, v) -> p.id = v, "id");
//        attribute("firstName", p -> p.firstName, (p, v) -> p.firstName = v, "first_name");
//        attribute("lastName", p -> p.lastName, (p, v) -> p.lastName = v, "last_name");
//        // attribute("id", p -> p.id, (p, id) -> p.id = id, "id", SELECT)
//        // filter("id", p -> p.id)
//        
//        
//    }};
//    
//    private static final ViewR<EntityQueryComposer<?, Person>> VR_PEOPLE = (List<?> attributes) -> {
//        EntityQueryComposer<?, Person> epc = new EntityQueryComposer<>(PERSON_TYPE, SqlDQML.SELECT, TB_PEOPLE.getSelectTemplate());
//        epc.requireAll(attributes);
//        return epc;
//    };
//    
//    @Test
//    public void test_entity_query_composer() {
//        EntityQueryComposer<?, Person> epc = sql.select(FULL_NAME).from(VR_PEOPLE);
//    }
//    
//    @Test
//    public void test_simple_query() throws InterruptedException, ExecutionException, MiException {
//        sql.select(FULL_NAME);
////        List<Person> people = sql.select(FULL_NAME)
////                .from(PEOPLE)
////                .inCity("City 1")
////                .orderByName()
////                .asList().execute();
//        /*
//        
//        List<Person> people = SQL.select(FULL_NAME)
//                        .from(PEOPLE)
//                        .inCity("City 1")
//                        .orderByName()
//                        .asList().submit(connection);
//                        // .submit(connection)
//                        // .asMap(p -> p.getName())
//        
//        */
//    }
//    
//    public static interface SelectPeople extends EntityQuery<Person> {
//        
//        default SelectPeople inCity(String city) {
//            return this;
//        }
//        
//        default SelectPeople orderByName() {
//            return this;
//        }
//    }
//    
}
