package org.cthul.miro.map;

import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Address;
import static org.hamcrest.Matchers.*;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MappedCUDTest {

    private MiConnection cnn = new MiConnection(TestDB.getConnection());
    
    @Before
    public void setUpClass() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City1");
        TestDB.insertAddress(1, "Street 1", "City2");
        TestDB.insertAddress(2, "Street 2", "City2");
    }
    
    @After
    public void tearDownClass() {
        TestDB.clear();
    }
    
    private Address byId(int id) throws SQLException {
        return Address.VIEW
                .select().where("id =", id)
                .execute(cnn).getSingle();
    }

    
    @Test
    public void test_execute_insert() throws Exception {
        
        Address a1 = new Address("Street 3", "City1");
        Address a2 = new Address("Street 3", "City2");
        
        Address.VIEW
                .insert()
                .values(a1, a2)
                .execute(cnn);
        
        assertThat(a1.getId(), is(greaterThan(0)));
        assertThat(a2.getId(), is(greaterThan(a1.getId())));
        
        Address a2b = byId(a2.getId());
        assertThat(a2b, equalTo(a2));
    }
    
    @Test
    public void test_execute_update() throws Exception {
        Address a1 = byId(1);
        a1.setStreet("Street z");
        Address a2 = byId(2);
        a2.setStreet("Street z");
        
        Address.VIEW
                .update("street")
                .values(a1, a2)
                .execute(cnn);
        
        Address a1b = byId(a1.getId());
        assertThat(a1b, equalTo(a1));
        Address a2b = byId(a2.getId());
        assertThat(a2b, equalTo(a2));
    }
    
    @Test
    public void test_execute_delete() throws Exception {
        Address a1 = byId(1);
        Address a2 = byId(2);
        
        Address.VIEW
                .delete()
                .values(a1, a2)
                .execute(cnn);
        
        assertThat(byId(a1.getId()), is(nullValue()));
        assertThat(byId(a2.getId()), is(nullValue()));
    }
}
