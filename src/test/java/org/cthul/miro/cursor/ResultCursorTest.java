package org.cthul.miro.cursor;

import org.cthul.miro.MiConnection;
import org.cthul.miro.test.TestDB;
import org.cthul.miro.test.model.Person0;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ResultCursorTest {
    
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
    public void test_cursor() throws Exception {
        Person0.TQuery qry = Person0.VIEW.select(cnn, "firstName");
        try (ResultCursor<Person0> result = qry.asCursor()._execute()) {
            Person0 cursor = null;
            String[] expectedNames = {"Jon", "Jane", "Alice", "Bob"};
            int i = 0;
            for (Person0 p: result) {
                assertThat("Person " + i, p.getFirstName(), is(expectedNames[i]));
                if (cursor == null) cursor = p;
                assertThat(p, sameInstance(cursor));
                i++;
            }
        }
    }
}