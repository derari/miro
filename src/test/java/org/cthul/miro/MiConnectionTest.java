package org.cthul.miro;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class MiConnectionTest {

    private MiConnection cnn;
    private boolean locked;

    @Before
    public void setUp() {
        cnn = new MiConnection(null);
    }

    @Test
    public void test() throws Exception {
        locked = true;
        final Object lock = new Object();
        MiFutureAction<Object, String> toString;
        toString = new MiFutureAction<Object, String>() {
            @Override
            public String call(Object arg) throws Exception {
                synchronized (lock) {
                    if (locked) lock.wait();
                }
                Thread.sleep(10);
                return String.valueOf(arg);
            }
        };
        
        MiFuture<String> futureString = cnn.submit(42, toString);
        assertThat(futureString.isDone(), is(false));
        synchronized (lock) {
            locked = false;
            lock.notifyAll();
        }
        
        futureString.waitUntilDone();
        assertThat(futureString.isDone(), is(true));
        assertThat(futureString.get(), is("42"));
    }
}