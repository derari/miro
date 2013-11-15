package org.cthul.miro.util;

import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.test.TestDB;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class LazyActionTest {
    
    private final int ACTION_PARAM = 5;
    private final int ACTION_RESULT = 15;
    
    private final MiConnection cnn = new MiConnection(TestDB.getConnection());
    private LazyAction<Integer> la = new LazyAction<>(cnn, ACTION_PARAM, new MiFutureAction<Integer, Integer>() {
        @Override
        public Integer call(Integer arg) throws Exception {
            lazyInit = arg;
            return ACTION_RESULT;
        }
    }) ;
    
    private int lazyInit = 0;
    
    @Test
    public void test_() {
        la.onComplete(new MiFutureAction<MiFuture<Integer>, Void>() {
            @Override
            public Void call(MiFuture<Integer> arg) throws Exception {
                assertThat(lazyInit, is(ACTION_PARAM));
                assertThat(arg.getResult(), is(ACTION_RESULT));
                return null;
            }
        });
        la.beDone();
    }    
}