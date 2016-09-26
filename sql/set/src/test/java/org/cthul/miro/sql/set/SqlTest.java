package org.cthul.miro.sql.set;

import java.util.concurrent.ExecutionException;
import static org.cthul.matchers.Fluents.*;
import org.cthul.miro.db.MiException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SqlTest {
    
    AddressBookDB db;

    @Before
    public void setUp() {
        TestDB.scenario1();
        db = new AddressBookDB(TestDB.getMiConnection());
    }
    
    @Test
    public void test1() throws MiException, InterruptedException, ExecutionException {
        People people = db.people().withFirstName("Bob");
        Person bob = people.result().getSingle();
        assertThat(bob.lastName).is("Brown");
    }
    
    @Test
    public void test1b() throws MiException, InterruptedException, ExecutionException {
        People people = db.people().withName("Bob", "Brown");
        Person bob = people.result().getSingle();
        assertThat(bob.lastName).is("Brown");
    }
    
    @Test
    public void test1c() throws MiException, InterruptedException, ExecutionException {
        People people = db.people().withFirstName("Bob").withLastName("Brown");
        Person bob = people.result().getSingle();
        assertThat(bob.lastName).is("Brown");
    }
    
    @Test
    public void test2() {
        Address a = db.addresses()
                .cityLike("C%1")//.cityLike("C%")
                .result()._getSingle();
        assertThat(a.city).is("City 1");
    }
    
    @Test
    public void test3() throws InterruptedException, ExecutionException, MiException {
        MappedSqlType<Person> type = new MappedSqlType<>(Person.class);
        type.column("firstName").field("firstName")
            .column("lastName").get(p -> p.lastName).set((Person p, String n) -> p.lastName = n);
        PeopleDao2 query = new PeopleDao2(TestDB.getMiConnection(), type);
        Person p = query
                .selectFirstName().selectLastName()
                .withId(1).result().getSingle();
        assertThat(p.firstName).is("John");
    }
    
    @Test
    public void test_nested_doSafe() throws InterruptedException, ExecutionException, MiException {
        MappedSqlType<Person> type = new MappedSqlType<>(Person.class);
        type.column("firstName").field("firstName")
            .column("lastName").get(p -> p.lastName).set((Person p, String n) -> p.lastName = n);
        PeopleDao2 query = new PeopleDao2(TestDB.getMiConnection(), type);
        Person p = query
                .selectFirstAndLast()
                .withId(1).result().getSingle();
        assertThat(p.firstName).is("John");
    }
}