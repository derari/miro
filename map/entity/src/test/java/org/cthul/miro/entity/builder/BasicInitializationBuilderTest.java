package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityInitializer;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 *
 */
public class BasicInitializationBuilderTest {
    
    public BasicInitializationBuilderTest() {
    }
    
    boolean initialized = false;
    boolean completed = false;
    boolean closed = false;

    public void setInitialized() {
        this.initialized = true;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public void setClosed() {
        this.closed = true;
    }

    @Test
    public void test_initializes() throws MiException {
        EntityInitializer<Object> init = Entities.buildInitializer(b -> b.addInitializer(o -> setInitialized()));
        init.apply(null);
        assertThat(initialized, is(true));
    }
    
    @Test
    public void test_completes() throws MiException {
        EntityInitializer<Object> init = Entities.buildInitializer(b -> b.addCompletable(() -> setCompleted()));
        init.complete();
        assertThat(completed, is(true));
    }
    
    @Test
    public void test_closes() throws MiException {
        EntityInitializer<Object> init = Entities.buildInitializer(b -> b.addCloseable(() -> setClosed()));
        init.close();
        assertThat(closed, is(true));
    }
    
    @Test
    public void test_complete_on_close() throws MiException {
        EntityInitializer<Object> init = Entities.buildInitializer(b -> b.addCompletable(() -> setCompleted()));
        init.close();
        assertThat(completed, is(true));
    }
    
    @Test
    public void test_no_unneeded_wrapper() throws MiException {
        EntityInitializer<Object> init = o -> setInitialized();
        EntityInitializer<Object> init2 = Entities.buildInitializer(b -> b.add(init));
        assertThat(init2, is(init));
    }
    
    @Test
    public void test_composite() throws MiException {
        EntityInitializer<Object> init = o -> setInitialized();
        EntityInitializer<Object> init2 = Entities.buildInitializer(b -> b.add(init).addCompletable(() -> setCompleted()));
        init2.complete();
        assertThat(completed, is(true));
    }
}
