package org.cthul.miro.dsl;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Address;
import org.cthul.miro.test.model.Person0;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class QueryWithTemplateTest {

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
    
    @AfterClass
    public static void tearDownClass() {
        TestDB.clear();
    }
    
    MiConnection cnn = new MiConnection(TestDB.getConnection());
    
    public void tearDown() throws Exception {
        cnn.close();
    }
    
    @Test
    public void test_select_all() throws Exception {
        Address a = cnn.select().from(Address.VIEW).getFirst().execute();
        assertThat(a.getId(), is(not(-1)));
        assertThat(a.getStreet(), is(notNullValue()));
        assertThat(a.getCity(), is(notNullValue()));
    }
    
    @Test
    public void test_select_explicit() throws Exception {
        Address a = cnn.select("id", "city").from(Address.VIEW).getFirst().execute();
        assertThat(a.getId(), is(not(-1)));
        assertThat(a.getStreet(), is(nullValue()));
        assertThat(a.getCity(), is(notNullValue()));
    }
    
    @Test
    public void test_where() throws Exception {
        List<Address> a = cnn.select()
                .from(Address.VIEW)
                .where().inCity("City2")
                .asList().execute();
        assertThat(a, hasSize(2));
    }
    
    @Test
    public void test_auto_join_unused() throws Exception {
        Person0.Query qry = cnn.select("id", "lastName").from(Person0.VIEW);
        assertThat(qry.getQueryString(), not(containsString("Addresses")));
        Person0 p = qry.getFirst().execute();
        assertThat(p.getId(), is(not(-1)));
        assertThat(p.getFirstName(), is(nullValue()));
        assertThat(p.getLastName(), is(notNullValue()));
        assertThat(p.getStreet(), is(nullValue()));
        assertThat(p.getCity(), is(nullValue()));
    }
    
    @Test
    public void test_auto_join_selected() throws Exception {
        Person0.Query qry = cnn.select("id", "lastName", "city").from(Person0.VIEW);
        assertThat(qry.getQueryString(), containsString("Addresses"));
        Person0 p = qry.getFirst().execute();
        assertThat(p.getId(), is(not(-1)));
        assertThat(p.getFirstName(), is(nullValue()));
        assertThat(p.getLastName(), is(notNullValue()));
        assertThat(p.getStreet(), is(nullValue()));
        assertThat(p.getCity(), is(notNullValue()));
    }
    
    @Test
    public void test_auto_join_filtered() throws Exception {
        Person0.Query qry = cnn.select("id", "lastName")
                                .from(Person0.VIEW)
                                .where().inCity("City2");
        assertThat(qry.getQueryString(), containsString("Addresses"));
        Person0 p = qry.getFirst().execute();
        assertThat(p.getId(), is(not(-1)));
        assertThat(p.getFirstName(), is(nullValue()));
        assertThat(p.getLastName(), is(notNullValue()));
        assertThat(p.getStreet(), is(nullValue()));
        assertThat(p.getCity(), is(nullValue()));
    }
}