package org.cthul.miro.util;

import java.util.List;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class GenericsUtilsTest {
    
    @Test
    public void test_explicit() {
        Class<?> fooClass = GenericsUtils.returnType(ExplicitFooFactory.class, "create");
        assertThat(fooClass, is(FooClass));
    }
    
    @Test
    public void test_implicit() {
        Class<?> fooClass = GenericsUtils.returnType(ImplicitFooFactory.class, "create");
        assertThat(fooClass, is(FooClass));
    }
    
    @Test
    public void test_indirect() {
        Class<?> fooClass = GenericsUtils.returnType(IndirectFooFactory.class, "create");
        assertThat(fooClass, is(FooClass));
    }
    
    @Test
    public void test_complex_type() {
        Class<?> fooClass = GenericsUtils.returnType(ListFactory.class, "create");
        assertThat(fooClass, is((Object) List.class));
    }
    
    static interface Factory<T> {
        T create();
    }
    
    static interface ExplicitFooFactory extends Factory<Foo> {
        @Override
        Foo create();
    }
    
    static interface ImplicitFooFactory extends Factory<Foo> {
    }
    
    static interface Indirection<S, T> extends Factory<T> {
    }
    
    static interface IndirectFooFactory<S> extends Indirection<S, Foo> {
    }
    
    static interface ListFactory extends Factory<List<Foo>> {
    }
    
    static final Object FooClass = Foo.class;
    
    static class Foo { }
}