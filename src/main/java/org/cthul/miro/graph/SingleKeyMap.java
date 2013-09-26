package org.cthul.miro.graph;

/**
 *
 */
public class SingleKeyMap<E extends Exception> extends KeyMap<Object, Object, E> {

    public SingleKeyMap(Fetch<Object, E> fetch) {
        super(fetch);
    }

    @Override
    protected Object prepareInternKey() {
        return null;
    }

    @Override
    protected Object internKey(Object prepared, Object key) {
        return key;
    }

    @Override
    protected Object internKey(Object key) {
        return key;
    }
}
