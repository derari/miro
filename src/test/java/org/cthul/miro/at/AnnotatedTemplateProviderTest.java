package org.cthul.miro.at;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Person0;
import org.cthul.miro.test.model.Person0.AtQuery;
import org.cthul.miro.test.model.Person0.AtQuery2;
import org.cthul.miro.view.ViewR;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class AnnotatedTemplateProviderTest {
    
    private ViewR<AtQuery> Persons = Person0.AT_VIEW;
    private ViewR<AtQuery2> Persons2 = Person0.AT_VIEW2;
    
    @BeforeClass
    public static void setUpClass() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City1");
        TestDB.insertAddress(1, "Street 1", "City2");
        TestDB.insertAddress(2, "Street 2", "City2");
        TestDB.insertPerson(0, "John",   "Doe", 0);
        TestDB.insertPerson(1, "Jane",  "Doe", 1);
        TestDB.insertPerson(2, "Alice", "Johnson", 2);
        TestDB.insertPerson(3, "Bob",   "Johnson", 2);
    }
    
    private MiConnection cnn = new MiConnection(TestDB.getConnection());

    @Test
    public void test_select_simple() {
        AtQuery qry = Persons.select("id", "firstName");
        assertThat(qry.toQueryString(cnn).getQueryString(),
                is("SELECT p.id AS id, first_name AS firstName FROM People p"));
        assertThat(qry.toQueryString(cnn).getArguments(0), empty());
        assertThat(qry._execute(cnn)._asList(), hasSize(4));
    }

    @Test
    public void test_select_all_auto_join() {
        AtQuery qry = Persons.select("*");
        assertThat(qry.toQueryString(cnn).getQueryString(),
                is("SELECT p.id AS id, first_name AS firstName, last_name AS lastName, "
                +          "a.street AS street, a.city AS city "
                + "FROM People p JOIN Addresses a ON p.address_id = a.id"));
        assertThat(qry.toQueryString(cnn).getArguments(0), empty());
        assertThat(qry._execute(cnn)._asList(), hasSize(4));
    }

    @Test
    public void test_where_auto_join() {
        AtQuery qry = Persons.select("id, firstName")
                .where().inCity("City2");
        assertThat(qry.toQueryString(cnn).getQueryString(),
                is("SELECT p.id AS id, first_name AS firstName "
                + "FROM People p JOIN Addresses a ON p.address_id = a.id "
                + "WHERE a.city = ?"));
        assertThat(qry.toQueryString(cnn).getArguments(0), contains((Object) "City2"));
        assertThat(qry._execute(cnn)._asList(), hasSize(3));
    }
    
    @Test
    public void test_config_with_mixed_args() {
        AtQuery qry = Persons.select("id, lastName")
                .with().firstNameAs("<unknown>")
                .where().id(1);
        Person0 p = qry._execute(cnn)._getSingle();
        assertThat(p.getId(), is(1));
        assertThat(p.getLastName(), is("Doe"));
        assertThat(p.getFirstName(), is("<unknown>"));
    }
    
    @Test
    public void test_impl() {
        AtQuery qry = Persons.select("id, lastName")
                .where().impl_inCity2("Street 2");
        List<Person0> result = qry._execute(cnn)._asList();
        assertThat(result, hasSize(2));
        assertThat(Person0.lastAddress, is("City2, Street 2"));
    }
    
    @Test
    public void test_class_impl() {
        AtQuery qry = Persons.select("id, lastName")
                .where().impl_atAddress("City2", "Street 1");
        List<Person0> result = qry._execute(cnn)._asList();
        assertThat(result, hasSize(1));
        assertThat(Person0.lastAddress, is("City2, Street 1"));
    }
    
    @Test
    public void test_order() {
        AtQuery qry = Persons.select().orderByName();
        List<Person0> result = qry._execute(cnn)._asList();
        Person0 p1 = result.get(0);
        assertThat(p1.getFirstName(), is("John"));
    }
    
    @Test
    public void test_always() {
        AtQuery qry = Persons2.select();
        Person0 p1 = qry._execute(cnn)._getFirst();
        assertThat(p1.getSomeFlag(), is("true"));
    }
}