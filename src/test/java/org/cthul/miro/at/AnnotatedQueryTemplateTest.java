package org.cthul.miro.at;

import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Person0;
import org.cthul.miro.test.model.Person0.AtQuery;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.cthul.miro.dsl.Select.*;

/**
 *
 */
public class AnnotatedQueryTemplateTest {
    
    private View<AtQuery> Persons = Person0.AT_VIEW;
    
    @BeforeClass
    public static void setUpClass() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City1");
        TestDB.insertAddress(1, "Street 1", "City2");
        TestDB.insertAddress(2, "Street 2", "City2");
        TestDB.insertPerson(0, "Jon",   "Doe", 0);
        TestDB.insertPerson(1, "Jane",  "Doe", 1);
        TestDB.insertPerson(2, "Alice", "Johnson", 2);
        TestDB.insertPerson(3, "Bob",   "Johnson", 2);
    }
    
    private MiConnection cnn = new MiConnection(TestDB.getConnection());

    @Test
    public void test_select_simple() {
        AtQuery qry = select("id", "firstName").from(Persons);
        assertThat(qry.getQueryString(),
                is("SELECT p.id, firstName FROM People p"));
        assertThat(qry.getArguments(), empty());
        assertThat(qry.asList()._execute(cnn), hasSize(4));
    }

    @Test
    public void test_select_all_auto_join() {
        AtQuery qry = select("*").from(Persons);
        assertThat(qry.getQueryString(),
                is("SELECT p.id, firstName, lastName, a.street, a.city "
                + "FROM People p JOIN Addresses a ON p.addressId = a.id"));
        assertThat(qry.getArguments(), empty());
        assertThat(qry.asList()._execute(cnn), hasSize(4));
    }

    @Test
    public void test_where_auto_join() {
        AtQuery qry = select("id, firstName")
                .from(Persons)
                .where().inCity("City2");
        assertThat(qry.getQueryString(),
                is("SELECT p.id, firstName "
                + "FROM People p JOIN Addresses a ON p.addressId = a.id "
                + "WHERE a.city = ?"));
        assertThat(qry.getArguments(), contains((Object) "City2"));
        assertThat(qry.asList()._execute(cnn), hasSize(3));
    }
}