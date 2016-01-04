package org.cthul.miro.view.impl;

import java.util.List;
import java.util.function.Function;
import org.cthul.miro.view.ViewCRUD;

/**
 *
 */
public class SimpleViewBuilder<C, R, U, D> {
    
    private Function<List<?>, R> selectFactory;

    public SimpleViewBuilder() {
    }
    
    public ViewCRUD<C, R, U, D> build() {
        return new SimpleView<>(selectFactory);
    }
    
    public <R1> SimpleViewBuilder<C, R1, U, D> select(Function<List<?>, R1> factory) {
        SimpleViewBuilder<C, R1, U, D> self = (SimpleViewBuilder) this;
        self.selectFactory = factory != null ? factory : NO_FACTORY;
        return self;
    }
    
    private static final Function NO_FACTORY = a -> {throw new UnsupportedOperationException();};
}
