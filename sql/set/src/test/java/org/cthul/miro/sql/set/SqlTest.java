package org.cthul.miro.sql.set;

import java.util.concurrent.ExecutionException;
import static org.cthul.matchers.fluent8.FluentAssert.*;
import org.cthul.miro.db.MiException;
import org.cthul.miro.map.MappedQuery;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.sql.SqlBuilder.Code;
import org.cthul.miro.sql.SqlDQML;
import org.cthul.miro.sql.map.MappedSelectRequest;
import org.cthul.miro.sql.map.MappedSqlType;
import org.cthul.miro.sql.syntax.MiSqlParser;
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
        type.property("firstName").column("firstName").field("firstName")
            .property("lastName").column("lastName").get(p -> p.lastName).set((Person p, String n) -> p.lastName = n);
        PeopleDao2 query = new PeopleDao2(TestDB.getMiConnection(), type);
        Person p = query
                .selectFirstName().selectLastName()
                .withId(1).result().getSingle();
        assertThat(p.firstName).is("John");
    }
    
    @Test
    public void test_nested_doSafe() throws InterruptedException, ExecutionException, MiException {
        MappedSqlType<Person> type = new MappedSqlType<>(Person.class);
        type.property("firstName").column("firstName").field("firstName")
            .property("lastName").column("lastName").get(p -> p.lastName).set((Person p, String n) -> p.lastName = n);
        PeopleDao2 query = new PeopleDao2(TestDB.getMiConnection(), type);
        Person p = query
                .selectFirstAndLast()
                .withId(1).result().getSingle();
        assertThat(p.firstName).is("John");
    }
    
    @Test
    public void test_copy_composer() throws InterruptedException, ExecutionException, MiException {
        Code<SelectBuilder> stmt = MiSqlParser.parsePartialSelect("FROM People WHERE id = 1");
        MappedSqlType<Person> type = new MappedSqlType<>(Person.class);
        type
//                .property("firstName")
//                .
//                .column("firstName")
                .attribute("firstName")
            .selectSnippet("q", s -> s.include(stmt))
            .property("lastName").column("lastName").field("lastName");
        
        MappedSelectRequest<Person> req = type.newMappedSelectComposer();
//        req.getFetchedProperties().add("firstName");
        req.getSelectComposer().getSelectedAttributes().add("firstName");
        MappedSelectRequest<Person> req2 = req.copy();
        req2.getSelectComposer().getMainView().addSnippet("q");
        
        MappedQuery<Person, SelectQuery> query1 = new MappedQuery(TestDB.getMiConnection(), SqlDQML.select());
        query1.apply(req);
        
        MappedQuery<Person, SelectQuery> query2 = new MappedQuery(TestDB.getMiConnection(), SqlDQML.select());
        query2.apply(req2);
        
        assertThat(query1.getStatement()).hasToString("SELECT firstName");
        assertThat(query2.getStatement()).hasToString("SELECT firstName FROM People WHERE id = 1");
    }
    
//    @Test
//    public void test_join() throws InterruptedException, ExecutionException, MiException {
//        People people = db.people().withFirstName("Bob").includeAddress();
//        Person bob = people.result().getSingle();
//        assertThat(bob.lastName).is("Brown");
//        assertThat(bob.address.city).is("Brown");
//    }
}
