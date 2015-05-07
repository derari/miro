package org.cthul.miro.futures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Test;

/**
 *
 */
public class SimpleMiActionTest {
    
    @Test
    public void test_initialization() {
        MyObject mo = new MyObject(5);
        assertThat(mo.getString(), is("5"));
    }
    
    static final MiConsumer<MyObject> INIT = MyObject::init;
    
    static class MyObject {
        
        final MiFuture<?> init = INIT.getTrigger(this);
        final int i;
        String s = null;

        public MyObject(int i) {
            this.i = i;
        }
        
        private void init() {
            s = String.valueOf(i);
        }

        public String getString() {
            init.awaitSuccess();
            return s;
        }
    }
    
}
