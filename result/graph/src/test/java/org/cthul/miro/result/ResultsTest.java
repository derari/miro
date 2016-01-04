package org.cthul.miro.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.EntityInitializer;
import org.cthul.miro.entity.EntityTypes;
import org.cthul.miro.graph.NodeSelector;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.impl.MiQueryQlBuilder;
import org.cthul.miro.db.jdbc.JdbcConnection;
import org.cthul.miro.db.sql.SqlClause;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.base.AttributeMapping;
import org.cthul.miro.entity.base.AttributeReader;
import org.cthul.miro.entity.base.NestedFactoryConfiguration;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultReadingEntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.impl.AbstractEntityNodeType;
import org.cthul.miro.test.Address;
import org.cthul.miro.test.Person;
import org.cthul.miro.test.TestDB;
import static org.cthul.miro.test.TestDB.clear;
import static org.cthul.miro.test.TestDB.insertAddress;
import static org.cthul.miro.test.TestDB.insertFriend;
import static org.cthul.miro.test.TestDB.insertPerson;
import static org.hamcrest.Matchers.*;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResultsTest {

    @BeforeClass
    public static void setUp() {
        clear();
        insertAddress(1, "Street 1", "City 1");
        insertAddress(2, "Street 2", "City 2");
        insertPerson(1, "John", "Doe", 1);
        insertPerson(2, "Jane", "Doe", 2);
//        insertPerson(3, "Bob", "Brown", 1);
        insertFriend(1, 2);
    }
    
    @AfterClass
    public static void tearDown() {
        TestDB.clear();
    }
    
    private final MiConnection connection = new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax());
    
    private final GraphSchema schema = GraphSchema.build()
                .put(Person.class, new PersonNode())
                .put(Address.class, new AddressNode());
    
    @Test
    public void test_simple_entity_type() throws InterruptedException, ExecutionException, MiException {
        Person p = query1().submit()
                .andThen(Results.build(PERSON_ENTITY))
                .getSingle();
        assertThat(p.firstName, is("John"));
    }
    
    private MiQueryString query1() {
        return MiQueryQlBuilder.create(connection)
                .ql("SELECT * FROM People WHERE id = ?")
                .pushArgument(1);
    };
    
    @Test
    public void test_foreign_key() throws InterruptedException, ExecutionException, MiException {
        Person p = query2().submit()
                .andThen(Results.build(PERSON_ENTITY).with(PERSON_ADDRESS))
                .getSingle();
        assertThat(p.firstName, is("John"));
        assertThat(p.address.street, is("Street 1"));
    }
    
    private MiQueryString query2() {
        return MiQueryQlBuilder.create(connection)
                .ql("SELECT p.id, p.first_name, p.last_name, "
                + "a.id AS address_id, a.street AS address_street, a.city AS address_city "
                + "FROM People p JOIN Addresses a ON p.address_id = a.id WHERE p.id = ?")
                .pushArgument(1);
    };
    
    @Test
    public void test_graph() throws MiException {
        try (Graph graph = schema.newGraph(connection);
                NodeSelector<Person> people = graph.nodeSelector(Person.class, "*", "address.*", "address.people.*")) {
            Person p = people.get(1);
            people.complete();
            assertThat(p.firstName, is("John"));
            assertThat(p.address.street, is("Street 1"));
            assertThat(p.address.people, contains(p));
        }
    }
    
    @Test
    public void test_graph_with_custom_query_and_manual_types() throws MiException, InterruptedException, ExecutionException {
        try (Graph graph = schema.newGraph(connection)) {
            EntityType<Person> personType = graph.<Person>entityType(Person.class).with(PERSON_FIELDS);
            EntityType<Address> addressType = graph.<Address>entityType(Address.class).with(ADDRESS_FIELDS);
            addressType = addressType.with(new AddressPersonConfig(personType));
            personType = personType.with(new PersonAddressConfig(addressType));
            
            Person p = query4().submit()
                    .andThen(Results.build(personType))
                    .getSingle();
            assertThat(p.firstName, is("John"));
            assertThat(p.address.street, is("Street 1"));
            assertThat(p.address.people, contains(p));
        }
    }
    
    private MiQueryString query4() {
        return MiQueryQlBuilder.create(connection)
                .ql("SELECT p.id, p.first_name, p.last_name, "
                + "a.id AS address_id, a.street AS address_street, a.city AS address_city, "
                + "p2.id AS address_p_id "
                + "FROM People p "
                + "JOIN Addresses a ON p.address_id = a.id "
                + "JOIN People p2 ON a.id = p2.address_id "
                + "WHERE p.id = ?")
                .pushArgument(1);
    }
    
    @Test
    public void test_graph_with_custom_query() throws MiException, InterruptedException, ExecutionException {
        try (Graph graph = schema.newGraph(connection)) {
            EntityType<Person> personType = graph.<Person>entityType(Person.class, "*", "address.*");
            
            Person p = graph.<Person>nodeSelector(Person.class).get(1);
            assertThat(p.firstName, is(nullValue()));
            
            query4().submit()
                    .andThen(Results.build(personType))
                    .noResult();
            assertThat(p.firstName, is("John"));
            assertThat(p.address.street, is("Street 1"));
            assertThat(p.address.people, contains(p));
        }
    }
    
    static final AttributeMapping<Person> PERSON_FIELDS = new AttributeMapping<Person>()
            .optional("first_name", (p, rs, i) -> p.firstName = rs.getString(i))
            .optional("last_name", (p, rs, i) -> p.lastName = rs.getString(i));
    static final PersonEntity PERSON_ENTITY = new PersonEntity();
    
    static final AttributeMapping<Address> ADDRESS_FIELDS = new AttributeMapping<Address>()
            .optional("street", (a, rs, i) -> a.street = rs.getString(i))
            .optional("city", (a, rs, i) -> a.city = rs.getString(i));
    static final AddressEntity ADDRESS_ENTITY = new AddressEntity();
    
    static final EntityConfiguration<Person> PERSON_ADDRESS = new PersonAddressConfig(ADDRESS_ENTITY);
    
    static class PersonEntity extends ResultReadingEntityType<Person> {

        public PersonEntity() {
            super(PERSON_FIELDS);
        }

        @Override
        protected Person newEntity(MiResultSet rs, int[] indices) throws MiException {
            Person p = new Person();
            p.id = rs.getInt(indices[0]);
            return p;
        }

        @Override
        protected int[] findColumns(MiResultSet rs) throws MiException {
            return ResultColumns.findAllColumns(rs, "id");
        }

        @Override
        public Person[] newArray(int length) {
            return new Person[length];
        }
    }
    
    static class PersonAddressConfig extends NestedFactoryConfiguration<Person, Address> {
        private final EntityType<Address> addressType;

        public PersonAddressConfig(EntityType<Address> addressType) {
            super("address = " + addressType);
            this.addressType = addressType;
        }
        
        @Override
        protected EntityFactory<Address> nestedFactory(MiResultSet rs) throws MiException {
            return addressType.newFactory(rs.subResult("address_"));
        }

        @Override
        protected void apply(Person person, EntityFactory<Address> factory) throws MiException {
            person.address = factory.newEntity();
        }
    }
    
    static class AddressEntity extends ResultReadingEntityType<Address> {

        public AddressEntity() {
            super(ADDRESS_FIELDS);
        }

        @Override
        protected Address newEntity(MiResultSet rs, int[] indices) throws MiException {
            Address a = new Address();
            a.id = rs.getInt(indices[0]);
            return a;
        }
        
        @Override
        protected int[] findColumns(MiResultSet rs) throws MiException {
            return ResultColumns.findAllColumns(rs, "id");
        }

        @Override
        public Address[] newArray(int length) {
            return new Address[length];
        }
    }
    
    static class AddressPersonConfig implements EntityConfiguration<Address> {
        private final EntityType<Person> personType;

        public AddressPersonConfig(EntityType<Person> personType) {
            this.personType = personType;
        }

        @Override
        public EntityInitializer<Address> newInitializer(MiResultSet resultSet) throws MiException {
            EntityFactory<Person> personFactory = personType.newFactory(resultSet.subResult("p_"));
            return new AttributeReader<Address>(resultSet)
                    .allOrNone("id", "p_id").set((a, rs, i) -> {
                        a.people = new ArrayList<>();
                        while (a.id == rs.getInt(i[0])) {
                            a.people.add(personFactory.newEntity());
                            if (!rs.next()) break;
                        }
                        rs.previous();
                    }).completeAndClose(personFactory);
        }
    }
    
    static class PersonNode extends AbstractEntityNodeType<Person> {

        @Override
        public Person[] newArray(int length) {
            return new Person[length];
        }

        @Override
        protected BatchLoader<Person> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            return new SimpleBatchLoader() {
                @Override
                protected EntityInitializer<Person> attributeInitializer(MiResultSet resultSet) throws MiException {
                    return newAttributeSetter(graph, attributes).newInitializer(resultSet);
                }
                @Override
                protected MiResultSet fetchAttributes(List<Object[]> keys) throws MiException {
                    return queryAttributes(graph, keys, attributes);
                }
            };
        }

        protected EntityConfiguration<Person> newAttributeSetter(GraphApi graph, List<?> attributes) throws MiException {
            EntityConfiguration<Person> config = PERSON_FIELDS;
            List<String> addressAttributes = new ArrayList<>();
            flattenStr(attributes).stream().filter(a -> a.startsWith("address.")).forEach(a -> {
                addressAttributes.add(a.substring(8));
            });
            if (!addressAttributes.isEmpty()) {
                config = config.and(
                        EntityTypes.subResultConfiguration("address_", 
                            new PersonAddressLink(graph, addressAttributes)));
            }
            return config;
        }

        protected MiResultSet queryAttributes(MiConnection cnn, List<Object[]> keys, List<?> attributes) throws MiException {
            MiQueryString query = MiQueryQlBuilder.create(cnn)
                    .ql("SELECT p.id, p.first_name, p.last_name, p.address_id "
                    + "FROM People p WHERE p.id")
                    .clause(SqlClause.IN, in -> in.list(keys.stream().map(k -> k[0])));
            return query.execute();
        }
        
        @Override
        public Person newEntity(Object[] key) {
            Person p = new Person();
            p.id = (Integer) key[0];
            return p;
        }

        @Override
        public Object[] getKey(Person e, Object[] array) {
            if (array == null) array = new Object[1];
            array[0] = e.id;
            return array;
        }

        @Override
        public KeyReader newKeyReader(MiResultSet rs) throws MiException {
            return newKeyReader(rs, "id");
        }
    }
    
    static class AddressNode extends AbstractEntityNodeType<Address> {

        @Override
        public Address[] newArray(int length) {
            return new Address[length];
        }

        @Override
        protected BatchLoader<Address> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            return new SimpleBatchLoader() {
                @Override
                protected EntityInitializer<Address> attributeInitializer(MiResultSet resultSet) throws MiException {
                    return newAttributeSetter(graph, attributes).newInitializer(resultSet);
                }
                @Override
                protected MiResultSet fetchAttributes(List<Object[]> keys) throws MiException {
                    return queryAttributes(graph, keys, attributes);
                }
            };
        }
        
        protected EntityConfiguration<Address> newAttributeSetter(GraphApi graph, List<?> attributes) throws MiException {
            return ADDRESS_FIELDS.and(new AddressPersonLink(graph, Arrays.asList("*")));
        }

        protected MiResultSet queryAttributes(MiConnection cnn, List<Object[]> keys, List<?> attributes) throws MiException {
            MiQueryQlBuilder query = MiQueryQlBuilder.create(cnn)
                    .ql("SELECT a.id, a.street, a.city, p.id AS p_id "
                    + "FROM Addresses a "
                    + "JOIN People p ON p.address_id = a.id "
                    + "WHERE a.id IN (?");
            for (int i = 1; i < keys.size(); i++) {
                query.ql(",?");
            }
            query.ql(") "
                    + "ORDER BY a.id");
            keys.forEach(o -> query.pushArgument(o[0]));
            return query.execute();
        }
        
        @Override
        public Address newEntity(Object[] key) {
            Address a = new Address();
            a.id = (Integer) key[0];
            return a;
        }

        @Override
        public Object[] getKey(Address e, Object[] array) {
            if (array == null) array = new Object[1];
            array[0] = e.id;
            return array;
        }

        @Override
        public KeyReader newKeyReader(MiResultSet rs) throws MiException {
            return newKeyReader(rs, "id");
        }
    }
    
    static class PersonAddressLink implements EntityConfiguration<Person> {

        private final GraphApi graph;
        private final List<String> addressAttributes;

        public PersonAddressLink(GraphApi graph, List<String> addressAttributes) {
            this.graph = graph;
            this.addressAttributes = addressAttributes;
        }

        @Override
        public EntityInitializer<Person> newInitializer(MiResultSet resultSet) throws MiException {
            NodeSelector<Address> addressSelector = graph.nodeSelector(Address.class, addressAttributes);
            return new AttributeReader<Person>(resultSet)
                    .required("id", (p, rs, index) -> {
                        int addressId = rs.getInt(index);
                        p.address = addressSelector.get(addressId);
            }).completeAndClose(addressSelector);
        }
    }
    
    static class AddressPersonLink implements EntityConfiguration<Address> {
        
        private final GraphApi graph;
        private final List<String> personAttributes;

        public AddressPersonLink(GraphApi graph, List<String> personAttributes) {
            this.graph = graph;
            this.personAttributes = personAttributes;
        }

        @Override
        public EntityInitializer<Address> newInitializer(MiResultSet resultSet) throws MiException {
            NodeSelector<Person> personSelector = graph.nodeSelector(Person.class, personAttributes);
            return new AttributeReader<Address>(resultSet)
                    .allOrNone("id", "p_id").set((a, rs, i) -> {
                        a.people = new ArrayList<>();
                        while (a.id == rs.getInt(i[0])) {
                            int pId = rs.getInt(i[1]);
                            a.people.add(personSelector.get(pId));
                            if (!rs.next()) break;
                        }
                        rs.previous();
                    }).completeAndClose(personSelector);
        }
    }
}
