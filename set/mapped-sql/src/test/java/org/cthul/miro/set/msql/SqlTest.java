package org.cthul.miro.set.msql;

import java.util.concurrent.ExecutionException;
import static org.cthul.matchers.Fluents.*;
import org.cthul.miro.at.model.EntitySchemaBuilder;
import org.cthul.miro.db.MiException;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.map.sql.MappedEntityType;
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
    public void test2() {
        Address a = db.addresses().cityLike("%1").cityLike("C%")
                .result()._getSingle();
        assertThat(a.city).is("City 1");
    }
    
    @Test
    public void test3() throws InterruptedException, ExecutionException, MiException {
        MappedEntityType<Person> type = new MappedEntityType<>(Person.class);
        type.initialize();
        type.getMappingBuilder()
                .field("firstName", p -> p.firstName, (p, n) -> p.firstName = n)
                .field("lastName", p -> p.lastName, (p, n) -> p.lastName = n);
        QueryableEntitySet<Person> entitySet = new QueryableEntitySet<>(type);
        entitySet.setConnection(TestDB.getMiConnection());
        PeopleDao2 query = new PeopleDao2(entitySet, type.getSelectLayer());
        Person p = query
                .selectFirstName().selectLastName()
                .withId(1).result().getSingle();
        assertThat(p.firstName).is("John");
    }
}
