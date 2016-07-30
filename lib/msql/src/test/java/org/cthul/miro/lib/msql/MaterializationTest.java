package org.cthul.miro.lib.msql;

import java.util.ArrayList;
import java.util.List;
import static org.cthul.matchers.fluent8.FluentAssert.assertThat;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.ext.jdbc.JdbcConnection;
import org.cthul.miro.result.Results;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.syntax.AnsiSqlSyntax;
import org.cthul.miro.test.Address;
import org.cthul.miro.test.Person;
import org.cthul.miro.test.TestDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cthul.miro.entity.map.ColumnMappingBuilder;

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
    private final SimpleSchema schema = new SimpleSchema();
    private final EntityType<Person> personType = schema.getEntityType(Person.class);
    private final EntityType<Address> addressType = schema.getEntityType(Address.class);
    
    {
        schema.setUp(Person.class)
                .any("a_id", "a_street", "a_city")
                .readWith(rs -> addressType.newFactory(rs.subResult("a_")))
                .field("address");
        schema.setUp(Address.class)
                .any("p_id", "p_first_name", "p_last_name")
                .readAs((rs, i) -> {
                    List<Person> list = new ArrayList<>();
                    
                    return list;
                });
    }
    
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
                       "a.id AS a_id, a.street AS a_street, a.city AS a_city " +
                "FROM People p " + 
                "JOIN Addresses a ON p.address_id = a.id " + 
                "WHERE p.id = ?", 1);
        
        Person p = qry.submit()
           .andThen(Results.build(personType))
           ._getSingle();
        
        assertThat(p.firstName).is("John");
        assertThat(p.address.street).is("Street 1");
    }
    
    
}
