package org.cthul.miro.view;

public abstract class AbstractViewCRUD<C, R, U, D> implements ViewCRUD<C, R, U, D> {

    private static final String[] NO_SELECT = {};

    @Override
    public C insert() {
        return insert(NO_SELECT);
    }

    @Override
    public R select() {
        return select(NO_SELECT);
    }

    @Override
    public U update() {
        return update(NO_SELECT);
    }
}
