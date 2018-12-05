package org.cthul.miro.sql.map;

import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.domain.Repository;
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.result.Results;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.test.Address;
import org.cthul.miro.test.Person;
import org.cthul.miro.test.TestDB;
import org.cthul.strings.JavaNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cthul.miro.entity.EntityTemplate;

/**
 *
 */
public class MaterializationTest {

    @BeforeClass
    public static void setUp() {
        TestDB.clear();
        TestDB.insertAddress(1, "Street 1", "City 1");
        TestDB.insertAddress(2, "Street 2", "City 2");
        TestDB.insertPerson(1, "John", "Doe", 1);
        TestDB.insertPerson(2, "Jane", "Doe", 2);
        TestDB.insertPerson(3, "Bob", "Brown", 1);
        TestDB.insertFriend(1, 2);
    }
    
    @AfterClass
    public static void tearDown() {
        TestDB.clear();
    }
    
    private final MiConnection connection = new JdbcConnection(TestDB::getConnection, new AnsiSqlSyntax());
    private final MappedSqlDomain schema = new MappedSqlDomain() {
        @Override
        protected String defaultColumnName(String propertyName) {
            return JavaNames.under_score(propertyName);
        }
        @Override
        protected String defaultAliasName(String columnName, String propertyName) {
            return columnName;
        }
        @Override
        protected String nestedPropertyPrefix(String columnName) {
            return columnName + "_";
        }
    };
    private final Repository repository = schema.newUncachedRepository(null);
    private final EntityTemplate<Person> personType = repository.getEntitySet(Person.class).getLookUp().andRead("*");
    private final EntityTemplate<Address> addressType = repository.getEntitySet(Address.class).getLookUp().andRead("*");
    
    @Test
    public void select_single_object() {
        SelectQuery qry = SelectQuery.create(connection);
        qry.sql("SELECT * FROM People WHERE id = ?", 1);
        
        Person p = qry.submit()
           .andThen(Results.build(personType))
           ._getSingle();
        
        assertThat(p.firstName).is("John");
    }
    
    @Test
    public void select_with_foreign_key() {
        SelectQuery qry = SelectQuery.create(connection);
        qry.sql("SELECT p.id, p.first_name, p.last_name, " +
                       "a.street AS address_street, a.city AS address_city " +
                "FROM People p " + 
                "JOIN Addresses a ON p.address_id = a.id " + 
                "WHERE p.id = ? " +
                "ORDER BY p.id", 1);
        
        Person p = qry.submit()
                .andThen(Results.build(personType))
                ._getSingle();
        
        assertThat(p.firstName).is("John");
        assertThat(p.address.street).is("Street 1");
    }
    
    @Test
    public void select_with_list() {
        SelectQuery qry = SelectQuery.create(connection);
        qry.sql("SELECT a.id, a.street, a.city, " +
                       "p.first_name AS people_first_name " +
                "FROM Addresses a " + 
                "JOIN People p ON p.address_id = a.id " + 
                "WHERE a.id = ? " +
                "ORDER BY a.id, p.id", 1);
        
        Address a = qry.submit()
                .andThen(Results.build(addressType))
                ._getSingle();
        
        assertThat(a.street).is("Street 1");
        assertThat(a.people).hasSize(2);
        assertThat(a.people.get(0).firstName).is("John");
    }
    
    @Test
    public void select_deep_with_graph() {
        
        SelectQuery qry = SelectQuery.create(connection);
        qry.sql("SELECT p.id, p.first_name, " + 
                       "a.id AS address_id, a.street AS address_street, a.city AS address_city, " +
                       "p2.id AS address_people_id " +
                "FROM People p " + 
                "JOIN Addresses a ON a.id = p.address_id " + 
                "JOIN People p2 ON p2.address_id = a.id " + 
                "WHERE p.id = ? " +
                "ORDER BY p.id, a.id, p2.id", 1);

        Repository graph = schema.newRepository(connection);
        EntityTemplate<Person> personGraph = graph.getEntitySet(Person.class).getLookUp().andRead("*");
        Person p = qry.submit()
                .andThen(Results.build(personGraph))
                ._getSingle();
        
        assertThat(p.firstName).is("John");
        assertThat(p.address.street).is("Street 1");
        Address a = p.address;
        assertThat(a.people).hasSize(2);
        assertThat(a.people.get(0)).is(p);
    }
    
}
