package org.cthul.miro.entity.builder;

import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.Entities;
import org.cthul.miro.entity.EntityFactory;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 *
 */
public class BasicFactoryBuilderTest {
    
    public BasicFactoryBuilderTest() {
    }
    
    static final Object VALUE = new Object();
    boolean completed = false;
    boolean closed = false;

    public void setCompleted() {
        this.completed = true;
    }

    public void setClosed() {
        this.closed = true;
    }

    @Test
    public void test_newEntity() throws MiException {
        EntityFactory<Object> fac = Entities.buildFactory(b -> b.setFactory(() -> VALUE));
        assertThat(fac.newEntity(), is(VALUE));
    }
    
    @Test
    public void test_completes() throws MiException {
        EntityFactory<Object> fac = Entities.buildFactory(b -> b.setFactory(() -> VALUE).addCompletable(() -> setCompleted()));
        fac.complete();
        assertThat(completed, is(true));
    }
    
    @Test
    public void test_closes() throws MiException {
        EntityFactory<Object> fac = Entities.buildFactory(b -> b.setFactory(() -> VALUE).addCloseable(() -> setClosed()));
        fac.close();
        assertThat(closed, is(true));
    }
    
    @Test
    public void test_complete_on_close() throws MiException {
        EntityFactory<Object> fac = Entities.buildFactory(b -> b.setFactory(() -> VALUE).addCompletable(() -> setCompleted()));
        fac.close();
        assertThat(completed, is(true));
    }
    
    @Test
    public void test_no_unneeded_wrapper() throws MiException {
        EntityFactory<Object> fac = () -> VALUE;
        EntityFactory<Object> fac2 = Entities.buildFactory(b -> b.set(fac));
        assertThat(fac2, is(fac));
    }
    
    @Test
    public void test_composite() throws MiException {
        EntityFactory<Object> fac = () -> VALUE;
        EntityFactory<Object> fac2 = Entities.buildFactory(b -> b.set(fac).addCompletable(() -> setCompleted()));
        fac2.complete();
        assertThat(completed, is(true));
    }
}
