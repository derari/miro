package org.cthul.miro.map;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.result.Results;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Address;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class MappedStatementTest {

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
    public void test_asList_execute() throws Exception {
        MiConnection cnn = new MiConnection(TestDB.getConnection());
        MappedStatementImpl ms = new MappedStatementImpl("Street 1");
        
        List<Address> result = ms.execute(cnn).asList();
        assertThat(result, hasSize(1));
        
        Address a = result.get(0);
        assertThat(a.getId(), is(0));
        assertThat(a.getStreet(), is("Street 1"));
        assertThat(a.getCity(), is("City"));
    }

    public class MappedStatementImpl extends MappedQueryString<Results<Address>> {
        public MappedStatementImpl(Object... args) {
            super(Address.MAPPING, "SELECT * FROM Addresses WHERE street = ?");
            select("id", "street", "city");
            batch(args);
        }
    }
}