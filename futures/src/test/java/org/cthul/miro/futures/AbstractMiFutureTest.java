package org.cthul.miro.futures;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 *
 */
public class AbstractMiFutureTest {
    
    private static ExecutorService executor;
    
    private TestFuture<String> instance;
    
    public AbstractMiFutureTest() {
    }
    
    @BeforeClass
    public static void beforeClass() {
        executor = Executors.newCachedThreadPool();
    }
    
    @AfterClass
    public static void afterClass() {
        executor.shutdownNow();
    }

    @Before
    public void setUp() {
        instance = new TestFuture<>(executor);
    }
    
    @Test
    public void test_cancel_first() {
        instance.cancel(false);
        assertThat(instance.isCancelled(), is(true));
        assertThat(instance.hasResult(), is(false));
    }
    
    @Test
    public void test_cancel_running() {
        instance.start();
        instance.cancel(false);
        assertThat(instance.isCancelled(), is(false));
        instance.result("");
        assertThat(instance.isCancelled(), is(true));
        assertThat(instance.hasResult(), is(false));
    }
    
    @Test
    public void test_cancel_chain() {
        MiFuture<String> f2 = instance.onComplete((f) -> "hello");
        instance.cancel(false);
        assertThat(f2.isCancelled(), is(true));
        assertThat(f2.hasResult(), is(false));
    }
    
    @Test
    public void test_shallow_cancel() {
        MiFuture<String> f2 = instance.onComplete((f) -> "hello");
        f2.cancel(false);
        assertThat(instance.isCancelled(), is(false));
    }
    
    @Test
    public void test_deep_cancel() {
        MiFuture<String> f2 = instance.onComplete((f) -> "hello");
        f2.deepCancel(false);
        assertThat(instance.isCancelled(), is(true));
    }

    @Test
    public void test_await_pass() throws Exception {
        TestSync sync = new TestSync(1);
        executor.execute(() -> {
            instance.result("hello");
            sync.countDown();
        });
        sync.await();
        instance.await();
        assertThat(instance.getResult(), is("hello"));
    }
    
    @Test
    public void test_await_block() throws Exception {
        TestSync sync = new TestSync(1);
        executor.execute(() -> {
            sync.awaitSlow();
            instance.result("hello");
        });
        sync.countDown();
        try {
            instance.await();
            assertThat(instance.getResult(), is("hello"));
        } catch (InterruptedException e) {
            assertThat(e.getMessage(), false);
        }
    }

    @Test
    public void test_await_timeout() throws Exception {
        TestSync sync = new TestSync(1);
        executor.execute(() -> {
            sync.await();
            instance.result("hello");
        });
        try {
            instance.await(10, TimeUnit.MILLISECONDS);
            assertThat(instance.getResult(), false);
        } catch (TimeoutException e) {
            // expected
        } finally {
            sync.countDown();
        }
    }

    @Test
    public void test_ensureIsDone() {
        try {
            instance.assertIsDone();
            assertThat("Expected exception", false);
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            instance.hasResult();
            assertThat("Expected exception", false);
        } catch (IllegalStateException e) {
            // expected
        }
        try {
            instance.hasFailed();
            assertThat("Expected exception", false);
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    @Test
    public void test_hasResult() {
        instance.result("hello");
        assertThat(instance.hasResult(), is(true));
        assertThat(instance.hasFailed(), is(false));
    }

    @Test
    public void test_hasFailed() {
        instance.fail(new RuntimeException());
        assertThat(instance.hasResult(), is(false));
        assertThat(instance.hasFailed(), is(true));
    }

    @Test
    public void test_getResult() {
        try {
            instance.getResult();
            assertThat("Expected exception", false);
        } catch (IllegalStateException e) {
            // expected
        }
        instance.result("hello");
        assertThat(instance.getResult(), is("hello"));
        assertThat(instance.getException(), is(nullValue()));
    }
    
    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void test_getException() {
        try {
            instance.getException();
            assertThat("Expected exception", false);
        } catch (IllegalStateException e) {
            // expected
        }
        instance.fail(new RuntimeException());
        assertThat(instance.getResult(), is(nullValue()));
        assertThat(instance.getException(), is(notNullValue()));
    }

    @Test
    public void test_onComplete() {
        MiFuture<Integer> f2 = instance.onComplete((f) -> {
            sleep(10);
            assertThat(f.get(), is("hello"));
            return 42;
        });
        instance.result("hello");
        assertThat(f2._get(), is(42));
    }
    
    @Test
    public void test_onSuccess() throws InterruptedException {
        MiFuture<Integer> f2 = instance.onSuccess((s) -> {
            return 42;
        });
        instance.fail(new RuntimeException());
        f2.await();
        assertThat(f2.hasFailed(), is(true));
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static class TestFuture<V> extends AbstractMiFuture<V> {

        public TestFuture() {
        }

        public TestFuture(Executor defaultExecutor) {
            super(defaultExecutor);
        }

        @Override
        protected void start() {
            super.start();
        }

        @Override
        protected void result(V result) {
            super.result(result);
        }

        @Override
        protected void fail(Throwable exception) {
            super.fail(exception);
        }
    }
    
    public static class TestSync extends CountDownLatch {

        public TestSync(int count) {
            super(count);
        }

        @Override
        public void await() {
            try {
                super.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public void awaitSlow() {
            await();
            sleep(10);
        }
    }
}
