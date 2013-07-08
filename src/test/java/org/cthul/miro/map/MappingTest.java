package org.cthul.miro.map;

import java.sql.ResultSet;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Address;
import org.junit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MappingTest {

    @BeforeClass
    public static void setUpClass() {
        TestDB.clear();
        TestDB.insertAddress(0, "Street 1", "City");
        TestDB.insertAddress(1, "Street 2", "City");
    }
    
    @AfterClass
    public static void tearDownClass() {
        TestDB.clear();
    }
    
    @Test
    public void test_newRecord() throws Exception {
        Address a = Address.MAPPING.newRecord(null);
        assertThat(a, is(notNullValue()));
    }
    
    @Test
    public void test_newEntityFactory() throws Exception {
        ResultBuilder.EntityFactory<Address> ef = Address.MAPPING.newEntityFactory();
        Address a = ef.newEntity();
        assertThat(a, is(notNullValue()));
    }
    
    @Test
    public void test_setField() throws Exception {
        Address a = new Address();
        Address.MAPPING.setField(a, "street", "The Street");
        assertThat(a.getStreet(), is("The Street"));
    }
    
    @Test
    public void test_newValueAdapter_select_street() throws Exception {
        Address a = new Address();
        try (ResultSet rs = TestDB.getConnection().createStatement()
                        .executeQuery(
                        "SELECT * FROM Addresses WHERE street = 'Street 1'")) {
            ResultBuilder.ValueAdapter va = Address.MAPPING.newValueAdapter("street");
            va.initialize(rs);
            rs.next();
            va.apply(a);
            va.complete();
        }
        
        assertThat(a.getStreet(), is("Street 1"));
    }
    
    @Test
    public void test_newValueAdapter_select_all() throws Exception {
        Address a = new Address();
        try (ResultSet rs = TestDB.getConnection().createStatement()
                        .executeQuery(
                        "SELECT * FROM Addresses WHERE street = 'Street 1'")) {
            ResultBuilder.ValueAdapter va = Address.MAPPING.newValueAdapter("id", "street", "city");
            va.initialize(rs);
            rs.next();
            va.apply(a);
            va.complete();
        }
        
        assertThat(a.getId(), is(0));
        assertThat(a.getStreet(), is("Street 1"));
        assertThat(a.getCity(), is("City"));
    }
}