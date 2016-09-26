package org.cthul.miro.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class CompleteAndClose implements Completable, AutoCloseable {
    
    public static boolean blank(Collection<?> c) {
        return c == null || c.isEmpty();
    }
    
    private final List<Object> names;
    private final List<Completable> completables;
    private final List<AutoCloseable> closeables;

    public CompleteAndClose(List<Object> names, List<Completable> completables, List<AutoCloseable> closeables) {
        this.names = blank(names) ? Collections.emptyList() : new ArrayList<>(names);
        this.completables = blank(completables) ? Collections.emptyList() : new ArrayList<>(completables);
        this.closeables = blank(closeables) ? Collections.emptyList() : new ArrayList<>(closeables);
    }
    
    @Override
    public void complete() throws Exception {
        Closeables.completeAll(completables);
    }

    @Override
    public void close() throws Exception {
        try {
            complete();
        } catch (Exception e) {
            throw Closeables.closeAll(e, closeables);
        }
        Closeables.closeAll(closeables);
    }

    @Override
    public String toString() {
        return names.stream().map(Object::toString).collect(Collectors.joining(","));
    }
    
    public static final CompleteAndClose NO_OP = new CompleteAndClose(null, null, null);
    
    public static class Builder<This extends CompletableBuilder> implements CompletableBuilder {

        private final List<Object> names = new ArrayList<>();
        private List<Completable> completables = null;
        private List<AutoCloseable> closeables = null;

        protected List<Object> getNames() {
            return names;
        }

        protected List<Completable> getCompletables() {
            return completables;
        }

        protected List<AutoCloseable> getCloseables() {
            return closeables;
        }

        @Override
        public This addCompletable(Completable completable) {
            if (completables == null) completables = new ArrayList<>();
            if (completable instanceof CompleteAndClose) {
                completables.addAll(((CompleteAndClose) completable).completables);
            } else {
                completables.add(completable);
            }
            return (This) this;
        }

        @Override
        public This addCloseable(AutoCloseable closeable) {
            if (closeables == null) closeables = new ArrayList<>();
            if (closeable instanceof CompleteAndClose) {
                closeables.addAll(((CompleteAndClose) closeable).closeables);
            } else {
                closeables.add(closeable);
            }
            return (This) this;
        }

        @Override
        public This addName(Object name) {
            if (name instanceof CompleteAndClose) {
                names.add(((CompleteAndClose) name).names);
            } else {
                names.add(name);
            }
            return (This) this;
        }
        
        public CompleteAndClose buildCompleteAndClose() {
            if (blank(getNames()) && blank(getCompletables()) && blank(getCloseables())) {
                return NO_OP;
            }
            return new CompleteAndClose(getNames(), getCompletables(), getCloseables());
        }

        @Override
        public String toString() {
            if (names.isEmpty()) return "[]";
            return names.stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
    }
    
    public static class NestedCompletableBuilder<This extends CompletableBuilder> implements CompletableBuilder {

        private final List<Object> names = new ArrayList<>();
        private final CompletableBuilder actual;

        public NestedCompletableBuilder(CompletableBuilder actual) {
            this.actual = actual;
        }

        @Override
        public This addCompletable(Completable completable) {
            actual.addCompletable(completable);
            return (This) this;
        }

        @Override
        public This addCloseable(AutoCloseable closeable) {
            actual.addCloseable(closeable);
            return (This) this;
        }

        @Override
        public This addName(Object name) {
            addNestedName(name);
            return (This) this;
        }
        
        protected void addNestedName(Object name) {
            if (name instanceof CompleteAndClose) {
                names.addAll(((CompleteAndClose) name).names);
            } else {
                names.add(name);
            }
        }

        public List<Object> getNames() {
            return names;
        }
        
        public CompleteAndClose buildCompleteAndClose() {
            if (blank(getNames())) {
                return NO_OP;
            }
            return new CompleteAndClose(getNames(), null, null);
        }

        @Override
        public String toString() {
            if (names.isEmpty()) return "[]";
            return names.stream().map(Object::toString)
                    .collect(Collectors.joining(", "));
        }
    }
}
