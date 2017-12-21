package org.cthul.miro.futures.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.cthul.miro.function.MiConsumer;
import org.cthul.miro.futures.MiFuture;
import org.cthul.miro.function.MiFutureFunction;
import org.cthul.miro.futures.MiResettableFuture;
import org.cthul.miro.function.MiSupplier;
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
    
    @Test
    public void test_wrapper() throws InterruptedException, ExecutionException {
        MiSupplier<String> aString = () -> "test 123";
        String quoted = 
                aString.asAction()
                .andThen(stringFuture)
                .getQuoted();
        assertThat(quoted, is("\"test 123\""));
    }
    
    @Test
    public void test_reset() throws InterruptedException, ExecutionException {
        AtomicInteger count = new AtomicInteger();
        AtomicInteger count2 = new AtomicInteger();
        MiSupplier<Integer> countSupplier = () -> count.getAndIncrement();
        MiResettableFuture<Integer> counter = countSupplier.getTrigger();
        assertThat(counter.get(), is(0));

        MiFuture<Integer> counter2 = counter.andThen(i -> count2.getAndIncrement());
        counter2.beDone();
        assertThat(count2.get(), is(1));
        assertThat(counter.get(), is(0));
        assertThat(count2.get(), is(1));
        
        counter.reset();
        assertThat(counter.get(), is(1));
        counter2.beDone();
        assertThat(count2.get(), is(2));
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
    
    MiFutureFunction<String, String, StringFuture> stringFuture = new MiFutureFunction<String, String, StringFuture>() {
        @Override
        public StringFuture wrap(MiFuture<? extends String> future) {
            return new StringFuture(future);
        }

        @Override
        public String call(String arg) throws Throwable {
            return arg;
        }
    };
    
    static class StringFuture extends MiFutureDelegator<String> {

        public StringFuture(MiFuture<? extends String> delegate) {
            super(delegate);
        }
        
        public String getQuoted() throws InterruptedException, ExecutionException {
            return '"' + get() + '"';
        }
    }
}
