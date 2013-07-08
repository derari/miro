package org.cthul.miro;

import java.sql.ResultSet;
import org.cthul.miro.test.TestDB;
import org.junit.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class MiPreparedStatementTest {

    private MiConnection cnn;

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
    
    @Before
    public void setUp() {
        cnn = new MiConnection(TestDB.getConnection());
    }
    
    @After
    public void tearDown() throws Exception {
        cnn.close();
    }

    /**
     * Submitting a prepared statement multiple times should not cause
     * problems with mixed arguments.
     */
    @Test
    public void test_submit() throws Exception {
        MiPreparedStatement ps = cnn.prepare("SELECT * FROM Addresses WHERE street = ?");
        MiFuture<ResultSet> f1 = ps.submitQuery(new Object[]{"Street 1"});
        MiFuture<ResultSet> f2 = ps.submitQuery(new Object[]{"Street 2"});
        
        ResultSet rs = f1.get();
        assertThat("There is a result", rs.next());
        assertThat(rs.getInt(1), is(0));
        assertThat("There is no other result", !rs.next());
        
        rs = f2.get();
        assertThat("There is a result", rs.next());
        assertThat(rs.getInt(1), is(1));
        assertThat("There is no other result", !rs.next());
    }
}