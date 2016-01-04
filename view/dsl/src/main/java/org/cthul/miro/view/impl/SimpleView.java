package org.cthul.miro.view.impl;

import java.util.List;
import java.util.function.Function;
import org.cthul.miro.view.ViewCRUD;

/**
 *
 */
public class SimpleView<C, R, U, D> implements ViewCRUD<C, R, U, D> {
    
    private final Function<List<?>, R> selectFactory;

    public SimpleView(Function<List<?>, R> selectFactory) {
        this.selectFactory = selectFactory;
    }

    @Override
    public C insert(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public R select(List<?> attributes) {
        return selectFactory.apply(attributes);
    }

    @Override
    public U update(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public D delete(List<?> attributes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static <C, R, U, D> SimpleViewBuilder<C, R, U, D> builder() {
        return new SimpleViewBuilder<>();
    }
}
