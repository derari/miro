package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Person0;
import static org.hamcrest.Matchers.*;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MappedSelectTest {
    
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
    
    @Test
    public void test_execute_singleSelect() throws Exception {
        MiConnection cnn = new MiConnection(TestDB.getConnection());
        
        List<Person0> list = Person0.VIEW
                .select("firstName")
                .where("id =", 2)
                .execute(cnn).asList();
        assertThat(list, hasSize(1));
        Person0 p = list.get(0);
        assertThat(p.getLastName(), is(nullValue()));
        assertThat(p.getFirstName(), is("Alice"));
    }
    
    @Test
    public void test_execute_with_join() throws Exception {
        MiConnection cnn = new MiConnection(TestDB.getConnection());
        
        List<Person0> list = Person0.VIEW
                .select()
                .where("firstName like", "%o%")
                .execute(cnn).asList();
        assertThat(list, hasSize(2));
        Person0 p = list.get(0);
        assertThat(p.getFirstName(), is("Jon"));
        assertThat(p.getStreet(), is("Street 1"));
        p = list.get(1);
        assertThat(p.getFirstName(), is("Bob"));
        assertThat(p.getStreet(), is("Street 2"));
    }
    
    @Test
    public void test_select_into() throws Exception {
        MiConnection cnn = new MiConnection(TestDB.getConnection());
        List<Person0> list = new ArrayList<>();
        list.add(new Person0(0));
        list.add(new Person0(3));
        Person0.VIEW
                .select()
                .into(list)
                .execute(cnn)
                .noResult();
        Person0 p = list.get(0);
        assertThat(p.getFirstName(), is("Jon"));
        assertThat(p.getStreet(), is("Street 1"));
        p = list.get(1);
        assertThat(p.getFirstName(), is("Bob"));
        assertThat(p.getStreet(), is("Street 2"));
    }
}
