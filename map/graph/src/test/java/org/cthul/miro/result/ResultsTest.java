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
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.map.AttributesConfiguration;
import org.cthul.miro.entity.base.MultiInitializer;
import org.cthul.miro.entity.base.NestedFactoryConfiguration;
import org.cthul.miro.entity.base.ResultColumns;
import org.cthul.miro.entity.base.ResultReadingEntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphApi;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.graph.impl.AbstractNodeType;
import org.cthul.miro.graph.impl.ColumnReader;
import org.cthul.miro.sql.SqlClause;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.test.Address;
import org.cthul.miro.test.Person;
import org.cthul.miro.test.TestDB;
import static org.cthul.miro.test.TestDB.clear;
import static org.cthul.miro.test.TestDB.insertAddress;
import static org.cthul.miro.test.TestDB.insertFriend;
import static org.cthul.miro.test.TestDB.insertPerson;
import static org.hamcrest.Matchers.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThat;

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
        return connection.newQuery().append(
                "SELECT * FROM People WHERE id = ?")
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
        return connection.newQuery().append(
                "SELECT p.id, p.first_name, p.last_name, "
                + "a.id AS address_id, a.street AS address_street, a.city AS address_city "
                + "FROM People p JOIN Addresses a ON p.address_id = a.id WHERE p.id = ?")
                .pushArgument(1);
    };
    
    @Test
    public void test_graph() throws MiException {
        try (Graph graph = schema.newGraph(connection);
                NodeSelector<Person> people = graph.newNodeSelector(Person.class, "*", "address.*", "address.people.*")) {
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
            EntityType<Person> personType = graph.getEntityType(Person.class, PERSON_FIELD_IDS);
            EntityType<Address> addressType = graph.<Address>getEntityType(Address.class, ADDRESS_FIELD_IDS);
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
        return connection.newQuery().append(
                "SELECT p.id, p.first_name, p.last_name, "
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
            EntityType<Person> personType = graph.<Person>getEntityType(Person.class, "*", "address.*");
            
            Person p = graph.<Person>newNodeSelector(Person.class).get(1);
            assertThat(p.firstName, is(nullValue()));
            
            query4().submit()
                    .andThen(Results.build(personType))
                    .noResult();
            assertThat(p.firstName, is("John"));
            assertThat(p.address.street, is("Street 1"));
            assertThat(p.address.people, contains(p));
        }
    }
    
    static final List<String> PERSON_FIELD_IDS = Arrays.asList("first_name", "last_name");
    static final AttributesConfiguration<Person, GraphApi> PERSON_FIELDS = new AttributesConfiguration<Person, GraphApi>()
            .optional("first_name").set((p, o) -> p.firstName = (String) o)
            .optional("last_name").set((p, o) -> p.lastName = (String) o);
    static final PersonEntity PERSON_ENTITY = new PersonEntity();
    
    static final List<String> ADDRESS_FIELD_IDS = Arrays.asList("street", "city");
    static final AttributesConfiguration<Address, GraphApi> ADDRESS_FIELDS = new AttributesConfiguration<Address, GraphApi>(Address.class)
            .optional("street").field("street")
            .optional("city").field("city");
    static final AddressEntity ADDRESS_ENTITY = new AddressEntity();
    
    static final EntityConfiguration<Person> PERSON_ADDRESS = new PersonAddressConfig(ADDRESS_ENTITY);
    
    static class PersonEntity extends ResultReadingEntityType<Person> {

        public PersonEntity() {
            super(PERSON_FIELDS.newConfiguration(null));
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
            super(ADDRESS_FIELDS.newConfiguration(null));
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
    }
    
    static class AddressPersonConfig implements EntityConfiguration<Address> {
        private final EntityType<Person> personType;

        public AddressPersonConfig(EntityType<Person> personType) {
            this.personType = personType;
        }

        @Override
        public EntityInitializer<Address> newInitializer(MiResultSet resultSet) throws MiException {
            EntityFactory<Person> personFactory = personType.newFactory(resultSet.subResult("p_"));
            AttributesConfiguration<Address, GraphApi> cfg = new AttributesConfiguration<Address, GraphApi>(Address.class)
                    .allOrNone("id", "p_id").readAs((rs, i) -> {
                        List<Person> list = new ArrayList<>();
                        int id = rs.getInt(i[0]);
                        while (id == rs.getInt(i[0])) {
                            list.add(personFactory.newEntity());
                            if (!rs.next()) break;
                        }
                        rs.previous();                        
                        return list;
                    }).field("people");
            return new MultiInitializer<Address>(resultSet)
                    .add(cfg.newConfiguration(null))
                    .completeAndClose(personFactory);
        }
    }
    
    static class PersonNode extends AbstractNodeType<Person> {

        @Override
        protected EntityConfiguration<Person> createAttributeReader(GraphApi graph, List<?> attributes) {
            EntityConfiguration<Person> config = PERSON_FIELDS.newConfiguration(graph, flattenStr(attributes));
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

        @Override
        protected BatchLoader<Person> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            return new SimpleBatchLoader(graph, attributes) {
                @Override
                protected MiResultSet fetchAttributes(List<Object[]> keys) throws MiException {
                    return queryAttributes(graph, keys, attributes);
                }
            };
        }

        protected MiResultSet queryAttributes(MiConnection cnn, List<Object[]> keys, List<?> attributes) throws MiException {
            MiQueryString query = cnn.newQuery().append(
                    "SELECT p.id, p.first_name, p.last_name, p.address_id "
                    + "FROM People p WHERE p.id")
                    .clause(SqlClause.in(), in -> in.list(keys.stream().map(k -> k[0])));
            return query.execute();
        }
        
        @Override
        public Person newEntity(GraphApi graph, Object[] key) {
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
        protected ColumnReader newKeyReader(MiResultSet resultSet, GraphApi graph) throws MiException {
            return ColumnReader.create(resultSet, "id");
        }
    }
    
    static class AddressNode extends AbstractNodeType<Address> {

        @Override
        protected EntityConfiguration<Address> createAttributeReader(GraphApi graph, List<?> attributes) {
            return ADDRESS_FIELDS.newConfiguration(graph, flattenStr(attributes))
                    .and(new AddressPersonLink(graph, Arrays.asList("*")));
        }

        @Override
        protected BatchLoader<Address> newBatchLoader(GraphApi graph, List<?> attributes) throws MiException {
            return new SimpleBatchLoader(graph, attributes) {
                @Override
                protected MiResultSet fetchAttributes(List<Object[]> keys) throws MiException {
                    return queryAttributes(graph, keys, attributes);
                }
            };
        }

        protected MiResultSet queryAttributes(MiConnection cnn, List<Object[]> keys, List<?> attributes) throws MiException {
            MiQueryString query = cnn.newQuery().append(
                    "SELECT a.id, a.street, a.city, p.id AS p_id "
                    + "FROM Addresses a "
                    + "JOIN People p ON p.address_id = a.id "
                    + "WHERE a.id IN (?");
            for (int i = 1; i < keys.size(); i++) {
                query.append(",?");
            }
            query.append(") "
                    + "ORDER BY a.id");
            keys.forEach(o -> query.pushArgument(o[0]));
            return query.execute();
        }
        
        @Override
        public Address newEntity(GraphApi graph, Object[] key) {
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
        protected ColumnReader newKeyReader(MiResultSet resultSet, GraphApi graph) throws MiException {
            return ColumnReader.create(resultSet, "id");
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
            NodeSelector<Address> addressSelector = graph.newNodeSelector(Address.class, addressAttributes);
            AttributesConfiguration<Person, GraphApi> cfg = new AttributesConfiguration<Person, GraphApi>(Person.class)
                    .require("id").mapToValue(o -> addressSelector.get((Integer) o))
                    .field("address");
            return new MultiInitializer<Person>(resultSet)
                    .add(cfg.newConfiguration(null))
                    .completeAndClose(addressSelector);
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
            NodeSelector<Person> personSelector = graph.newNodeSelector(Person.class, personAttributes);
            AttributesConfiguration<Address, GraphApi> cfg = new AttributesConfiguration<Address, GraphApi>(Address.class)
                    .allOrNone("id", "p_id").readAs((rs, i) -> {
                        List<Person> list = new ArrayList<>();
                        int id = rs.getInt(i[0]);
                        while (id == rs.getInt(i[0])) {
                            int pId = rs.getInt(i[1]);
                            list.add(personSelector.get(pId));
                            if (!rs.next()) break;
                        }
                        rs.previous();
                        return list;
                    }).field("people");
            return new MultiInitializer<Address>(resultSet)
                    .add(cfg.newConfiguration(null))
                    .completeAndClose(personSelector);
        }
    }
}
