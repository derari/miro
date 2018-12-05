package org.cthul.miro.result;

import java.util.*;
import java.util.concurrent.ExecutionException;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.entity.EntityConfiguration;
import org.cthul.miro.entity.EntityFactory;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.db.request.MiQueryString;
import org.cthul.miro.domain.Domain;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.domain.impl.AbstractEntityType;
import org.cthul.miro.domain.impl.ColumnReader;
import org.cthul.miro.entity.*;
import org.cthul.miro.entity.builder.SelectorBuilder;
import org.cthul.miro.entity.map.*;
import org.cthul.miro.entity.map.ResultColumns.ColumnRule;
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
    
    private final Domain schema = Domain.build()
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
        
        try (Repository graph = schema.newRepository(connection);
                EntitySelector<Person> people = graph.getEntitySet(Person.class).getSelector().andLoad("*", "address.*", "address.people.*")) {
            Person p = people.get(1);
            people.complete();
            assertThat(p.firstName, is("John"));
            assertThat(p.address.street, is("Street 1"));
            assertThat(p.address.people, contains(p));
        }
    }
    
    @Test
    public void test_graph_with_custom_query_and_manual_types() throws MiException, InterruptedException, ExecutionException {
        try (Repository graph = schema.newRepository(connection)) {
            EntityTemplate<Person> personType = graph.getEntitySet(Person.class).getLookUp().andLoad(PERSON_FIELD_IDS);
            EntityTemplate<Address> addressType = graph.getEntitySet(Address.class).getLookUp().andLoad(ADDRESS_FIELD_IDS);
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
        try (Repository graph = schema.newRepository(connection)) {
            EntityTemplate<Person> personType = graph.getEntitySet(Person.class).getLookUp().andLoad("*", "address.*");
            
            Person p = graph.getEntitySet(Person.class).getSelector().get(1);
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
    static final EntityProperties<Person> PERSON_FIELDS = new PropertiesConfiguration<Person>()
            .property("first_name").requiredColumn("first_name").set((p, o) -> p.firstName = (String) o)
            .property("last_name").requiredColumn("last_name").set((p, o) -> p.lastName = (String) o);
    static final PersonEntity PERSON_ENTITY = new PersonEntity();
    
    static final List<String> ADDRESS_FIELD_IDS = Arrays.asList("street", "city");
    static final EntityProperties<Address> ADDRESS_FIELDS = new PropertiesConfiguration<>(Address.class)
            .property("street").requiredColumn("street").field("street")
            .property("city").requiredColumn("city").field("city");
    static final AddressEntity ADDRESS_ENTITY = new AddressEntity();
    
    static final EntityConfiguration<Person> PERSON_ADDRESS = new PersonAddressConfig(ADDRESS_ENTITY);
    
    static class PersonEntity implements EntityTemplate<Person> {

        @Override
        public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Person> builder) throws MiException {
            int cId = ResultColumns.findColumn(ColumnRule.REQUIRED, resultSet, "id");
            builder.setFactory(Person::new)
                    .add(p -> p.id = resultSet.getInt(cId))
                    .add(PERSON_FIELDS.read(null), resultSet);
        }
    }
    
    static class PersonAddressConfig implements EntityConfiguration<Person> {
        private final EntityTemplate<Address> addressTemplate;

        public PersonAddressConfig(EntityTemplate<Address> addressTemplate) {
            this.addressTemplate = addressTemplate;
        }

        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Person> builder) throws MiException {
            EntityFactory<Address> addressFactory = builder.nestedFactory(addressTemplate, resultSet.subResult("address_"));
            builder.addName("address = " + addressFactory);
            builder.addInitializer(p -> p.address = addressFactory.newEntity());
        }

        @Override
        public String toString() {
            return "address = " + addressTemplate;
        }
    }
    
    static class AddressEntity implements EntityTemplate<Address> {

        @Override
        public void newFactory(MiResultSet resultSet, FactoryBuilder<? super Address> builder) throws MiException {
            int cId = ResultColumns.findColumn(ColumnRule.REQUIRED, resultSet, "id");
            builder.set(Address::new)
                .addInitializer(a -> a.id = resultSet.getInt(cId))
                .add(ADDRESS_FIELDS.read(null), resultSet);
        }
    }
    
    static class AddressPersonConfig implements EntityConfiguration<Address> {
        private final EntityTemplate<Person> personType;

        public AddressPersonConfig(EntityTemplate<Person> personType) {
            this.personType = personType;
        }

        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Address> builder) throws MiException {
            EntityFactory<Person> personFactory = builder.nestedFactory(personType, resultSet.subResult("p_"));
            PropertiesConfiguration<Address> cfg = new PropertiesConfiguration<>(Address.class)
                    .property("people").allOrNoneColumns("id", "p_id").read((rs, i) -> {
                        List<Person> list = new ArrayList<>();
                        int id = rs.getInt(i[0]);
                        while (id == rs.getInt(i[0])) {
                            list.add(personFactory.newEntity());
                            if (!rs.next()) break;
                        }
                        rs.previous();                        
                        return list;
                    }).field("people");
            builder.add(cfg.read(null), resultSet);
        }
    }
    
    static class PersonNode extends AbstractEntityType<Person> {

        @Override
        public void newEntityCreator(Repository repository, SelectorBuilder<Person> builder) {
            builder.setSelector(key -> {
                Person p = new Person();
                p.id = (Integer) key[0];
                return p;
            });
        }

        @Override
        protected EntityConfiguration<Person> createPropertiesReader(Repository repository, Collection<?> properties) {
            EntityConfiguration<Person> config = PERSON_FIELDS.read(repository, flattenStr(properties));
            List<String> addressAttributes = new ArrayList<>();
            flattenStr(properties).stream().filter(a -> a.startsWith("address.")).forEach(a -> {
                addressAttributes.add(a.substring(8));
            });
            if (!addressAttributes.isEmpty()) {
                config = config.and(Entities.subResultConfiguration("address_", 
                            new PersonAddressLink(repository, addressAttributes)));
            }
            return config;
        }

        @Override
        protected Object[] getKey(Person e, Object[] array) throws MiException {
            if (array == null) array = new Object[1];
            array[0] = e.id;
            return array;
        }

        @Override
        protected ColumnReader newKeyReader(Repository repository, MiResultSet resultSet) throws MiException {
            return ColumnReader.create(resultSet, "id");
        }

        @Override
        protected BatchLoader<Person> newBatchLoader(Repository repository, MiConnection connection, Collection<?> properties) {
            return new SimpleBatchLoader(repository, properties) {
                @Override
                protected MiResultSet fetchProperties(List<Object[]> keys) throws MiException {
                    return queryAttributes(connection, keys, flattenStr(properties));
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
        public ColumnMapping mapToColumns(String prefix, Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    static class AddressNode extends AbstractEntityType<Address> {

        @Override
        protected EntityConfiguration<Address> createPropertiesReader(Repository repository, Collection<?> properties) {
            return ADDRESS_FIELDS.read(repository, flattenStr(properties))
                    .and(new AddressPersonLink(repository, Arrays.asList("*")));
        }

        @Override
        protected BatchLoader<Address> newBatchLoader(Repository repository, MiConnection connection, Collection<?> properties) {
            return new SimpleBatchLoader(repository, properties) {
                @Override
                protected MiResultSet fetchProperties(List<Object[]> keys) throws MiException {
                    return queryAttributes(connection, keys, flattenStr(properties));
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
        public void newEntityCreator(Repository repository, SelectorBuilder<Address> selectorBuilder) {
            selectorBuilder.setSelector(key -> {
                Address a = new Address();
                a.id = (Integer) key[0];
                return a;
            });
        }

        @Override
        public Object[] getKey(Address e, Object[] array) {
            if (array == null) array = new Object[1];
            array[0] = e.id;
            return array;
        }

        @Override
        protected ColumnReader newKeyReader(Repository repository, MiResultSet resultSet) throws MiException {
            return ColumnReader.create(resultSet, "id");
        }
        
        @Override
        public ColumnMapping mapToColumns(String prefix, Object key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    static class PersonAddressLink implements EntityConfiguration<Person> {

        private final Repository repo;
        private final List<String> addressAttributes;

        public PersonAddressLink(Repository graph, List<String> addressAttributes) {
            this.repo = graph;
            this.addressAttributes = addressAttributes;
        }

        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Person> builder) throws MiException {
            EntitySelector<Address> addressSelector = repo.getEntitySet(Address.class).getSelector().andLoad(addressAttributes);
            PropertiesConfiguration<Person> cfg = new PropertiesConfiguration<>(Person.class)
                    .property("id").requiredColumn("id").mapToValue(o -> addressSelector.get((Integer) o))
                    .field("address");
            builder.addCompleteAndClose(addressSelector);
            cfg.newReader(repo, resultSet, builder);
        }
    }
    
    static class AddressPersonLink implements EntityConfiguration<Address> {
        
        private final Repository repo;
        private final List<String> personAttributes;

        public AddressPersonLink(Repository graph, List<String> personAttributes) {
            this.repo = graph;
            this.personAttributes = personAttributes;
        }

        @Override
        public void newInitializer(MiResultSet resultSet, InitializationBuilder<? extends Address> builder) throws MiException {
            EntitySelector<Person> personSelector = repo.getEntitySet(Person.class).getSelector().andLoad(personAttributes);
            PropertiesConfiguration<Address> cfg = new PropertiesConfiguration<>(Address.class)
                    .property("people").allOrNoneColumns("id", "p_id").read((rs, i) -> {
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
            builder.addCompleteAndClose(personSelector);
            cfg.newReader(repo, resultSet, builder);
        }
    }
}
