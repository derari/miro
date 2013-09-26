package org.cthul.miro.graph;

import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Address;
import org.cthul.miro.test.model.Person1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class GraphQueryTest {

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
//        Person1 p = cnn.select().from(Person1.VIEW).getFirst().execute();
//        assertThat(p.getId(), is(not(-1)));
//        assertThat(p.getFirstName(), is(notNullValue()));
//        assertThat(p.getLastName(), is(notNullValue()));
//        Address a = p.getAddress();
//        assertThat(a, is(notNullValue()));
//        assertThat(a.getStreet(), is(notNullValue()));
    }
    
    @Test
    public void test_instance_identity() throws Exception {
//        Person1[] pp = cnn.select().from(Person1.VIEW).byKeys(null, 2, 3)
//                .asOrderedArray().execute();
//        assertThat(pp[0].getAddress(), is(sameInstance(pp[1].getAddress())));
    }
    
}