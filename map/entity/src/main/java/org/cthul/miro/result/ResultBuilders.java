package org.cthul.miro.result;

import java.lang.reflect.Array;
import java.util.AbstractList;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.cthul.miro.result.cursor.ResultCursor;
import org.cthul.miro.result.cursor.ResultCursorBase;
import org.cthul.miro.util.Closeables;
import org.cthul.miro.entity.EntityTemplate;

/**
 * Default implementations of {@link EntityResultBuilder}.
 */
public class ResultBuilders {

    protected ResultBuilders() {
    }

    @SuppressWarnings("unchecked")
    public static <Entity> IteratorResult<Entity> getIteratorResult() {
        return IteratorResult.ITERATOR_RESULT;
    }

    public static <Entity> IteratorResult<Entity> getIteratorResult(Class<Entity> clazz) {
        return getIteratorResult();
    }

    public static class IteratorResult<Entity> implements EntityResultBuilder<ResultIterator<Entity>, Entity> {
        private static final IteratorResult ITERATOR_RESULT = new IteratorResult<>();

        @Override
        public ResultIterator<Entity> build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            return new ResultReader<>(rs, type);
        }
    }
    
    private static class ResultReader<Entity> implements ResultIterator<Entity> {
        
        private final MiResultSet rs;
        private final EntityFactory<? extends Entity> factory;
        private MiException ex = null;
        private boolean atNext = false;
        private boolean hasMore = true;
        private long rowId = 0;

        public ResultReader(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            this.rs = rs;
            this.factory = type.newFactory(rs);
        }

        @Override
        public boolean hasNext() {
            try {
                if (atNext && hasMore) return true;
                if (rs.next()) {
                    rowId++;
                    return atNext = true;
                } else {
                    return hasMore = false;
                }
            } catch (MiException e) {
                this.ex = e;
                return hasMore = false;
            }
        }
        
        @Override
        public Entity next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {
                Entity e = factory.newEntity();
                factory.complete();
                return e;
            } catch (MiException e) {
                this.ex = e;
                hasMore = false;
                throw new IllegalStateException(e);
            }
        }
        
        @Override
        public void close() throws MiException {
            if (ex != null) {
                throw Closeables.closeAll(ex, factory, rs);
            } else {
                Closeables.closeAll(MiException.class, factory, rs);
            }
        }
        
        @Override
        public Iterator<Entity> iterator() {
            return new Iterator<Entity>() {
                long myRow = rowId;
                @Override
                public boolean hasNext() {
                    if (myRow != rowId) {
                        throw new ConcurrentModificationException();
                    }
                    boolean n = ResultReader.this.hasNext();
                    myRow = rowId;
                    return n;
                }
                @Override
                public Entity next() {
                    hasNext();
                    return ResultReader.this.next();
                }
            };
        }
    }

    @SuppressWarnings("unchecked")
    public static <Entity> LazyListResult<Entity> getLazyListResult() {
        return LazyListResult.LIST_RESULT;
    }

    public static <Entity> LazyListResult<Entity> getLazyListResult(Class<Entity> clazz) {
        return getLazyListResult();
    }

    public static class LazyListResult<Entity> implements EntityResultBuilder<EntityList<Entity>, Entity> {
        
        private static final LazyListResult LIST_RESULT = new LazyListResult<>();

        @Override
        public EntityList<Entity> build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            return new LazyList<>(rs, type);
        }
    }
    
    private static class LazyList<Entity> extends AbstractList<Entity> implements EntityList<Entity> {
        
        private final List<Entity> data = new ArrayList<>();
        private EntityFactory<? extends Entity> factory;
        private MiResultSet rs;

        public LazyList(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            this.rs = rs;
            this.factory = type.newFactory(rs);
        }
        
        private boolean fetchIndex(int index) {
            int n = index - data.size();
            if (n < 0) return true;
            return more(n+1);
        }

        private synchronized boolean more(int n) {
            if (rs == null) return false;
            try {
                while (n-- > 0 && rs.next()) {
                    data.add(factory.newEntity());
                }
                factory.complete();
                if (rs.isAfterLast()) {
                    close();
                }
                return n < 0;
            } catch (MiException e) {
                try {
                    close();
                } catch (MiException e2) {
                    e.addSuppressed(e2);
                }  
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Entity get(int index) {
            fetchIndex(index);
            return data.get(index);
        }

        @Override
        public Iterator<Entity> iterator() {
            return new Iterator<Entity>() {
                int next = 0;
                @Override
                public boolean hasNext() {
                    return fetchIndex(next);
                }
                @Override
                public Entity next() {
                    return get(next++);
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty() && !more(1);
        }
        
        @Override
        public int size() {
            more(Integer.MAX_VALUE);
            return data.size();
        }

        @Override
        public void close() throws MiException {
            if (rs != null) {
                try {
                    Closeables.closeAll(MiException.class, factory, rs);
                } finally {
                    rs = null;
                    factory = null;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <Entity> ListResult<Entity> getListResult() {
        return ListResult.LIST_RESULT;
    }

    public static <Entity> ListResult<Entity> getListResult(Class<Entity> clazz) {
        return getListResult();
    }

    public static class ListResult<Entity> implements EntityResultBuilder<List<Entity>, Entity> {
        
        private static final ListResult LIST_RESULT = new ListResult<>();

        @Override
        public List<Entity> build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            final List<Entity> result = new ArrayList<>();
            try (MiResultSet __ = rs;
                    EntityFactory<? extends Entity> factory = type.newFactory(rs)) {
                while (rs.next()) {
                    final Entity record = factory.newEntity();
                    result.add(record);
                }
                factory.complete();
            }
            return result;
        }
    }
    
    public static <Entity> ArrayResult<Entity> getArrayResult(Entity[] ary) {
        return new ArrayResult<>(ary);
    }
    
    public static <Entity> ArrayResult<Entity> getArrayResult(Class<? super Entity> clazz) {
        return getArrayResult((Entity[]) Array.newInstance(clazz, 0));
    }

    public static class ArrayResult<Entity> implements EntityResultBuilder<Entity[], Entity> {

        private final Entity[] ary;
        private final EntityResultBuilder<? extends Collection<Entity>, Entity> listResult;

        public ArrayResult(Entity[] ary) {
            this(ary, getListResult());
        }
        
        public ArrayResult(Entity[] ary, EntityResultBuilder<? extends Collection<Entity>, Entity> listResult) {
            this.ary = ary;
            this.listResult = listResult;
        }

        @Override
        public Entity[] build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            Collection<Entity> result = listResult.build(rs, type);
            return result.toArray(ary);
        }
    }

    @SuppressWarnings("unchecked")
    public static <Entity> CursorResult<Entity> getCursorResult() {
        return CursorResult.CURSOR_RESULT;
    }

    public static <Entity> CursorResult<Entity> getCursorResult(Class<Entity> clazz) {
        return getCursorResult();
    }

    public static class CursorResult<Entity> implements EntityResultBuilder<ResultCursor<Entity>, Entity> {

        @SuppressWarnings("rawtypes")
        private static final CursorResult CURSOR_RESULT = new CursorResult<>();

        @Override
        public ResultCursor<Entity> build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            return new MappedResultCursor<>(rs, type);
        }
    }

    protected static class MappedResultCursor<Entity> extends ResultCursorBase<Entity> {

        private final MiResultSet rs;
        private final EntityFactory<? extends Entity> factory;
        private boolean nextIsExpected = true;
        private boolean isAtNext = false;

        @SuppressWarnings("LeakingThisInConstructor")
        public MappedResultCursor(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            this.rs = rs;
            this.factory = type.newFactory(rs);
//            setCursorValue(factory.newCursorValue(this));
        }

        @Override
        public boolean hasNext() {
            try {
                if (nextIsExpected) {
                    if (!isAtNext) {
                        nextIsExpected = rs.next();
                        isAtNext = nextIsExpected;
                    }
                }
                return nextIsExpected;
            } catch (MiException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Entity getFixCopy() {
//            try {
//                return factory.copy(cursor);
//            } catch (MiException e) {
//                throw new RuntimeException(e);
//            }
            return null;
        }

        @Override
        protected void makeNext() {
            if (!isAtNext) {
                hasNext();
                if (!isAtNext) {
                    throw new IllegalStateException("No next element");
                }
            }
            isAtNext = false;
//            try {
////                init.apply(cursor);
////                init.complete();
//            } catch (MiException e) {
//                throw new RuntimeException(e);
//            }
        }

        @Override
        public void close() {
//            Closables.uncheckedCloseAll(rs, factory, init);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <Entity> SingleResult<Entity> getSingleResult() {
        return SingleResult.SINGLE_RESULT;
    }

    public static <Entity> SingleResult<Entity> getSingleResult(Class<Entity> clazz) {
        return getSingleResult();
    }
    
    @SuppressWarnings("unchecked")
    public static <Entity> SingleResult<Entity> getFirstResult() {
        return SingleResult.FIRST_RESULT;
    }

    public static <Entity> SingleResult<Entity> getFirstResult(Class<Entity> clazz) {
        return getFirstResult();
    }

    public static class SingleResult<Entity> implements EntityResultBuilder<Entity, Entity> {

        @SuppressWarnings("rawtypes")
        private static final SingleResult SINGLE_RESULT = new SingleResult<>(true);

        @SuppressWarnings("rawtypes")
        private static final SingleResult FIRST_RESULT = new SingleResult<>(false, () -> null);

        private final boolean forceSingle;
        private final Supplier<Entity> defValue;

        public SingleResult(boolean forceSingle) {
            this.forceSingle = forceSingle;
            this.defValue = () -> {throw new NoSuchElementException();};
        }

        public SingleResult(boolean forceSingle, Supplier<Entity> defValue) {
            this.forceSingle = forceSingle;
            this.defValue = defValue;
        }

        @Override
        public Entity build(MiResultSet rs, EntityTemplate<? extends Entity> type) throws MiException {
            if (!rs.next()) {
                return defValue.get();
            }
            try (MiResultSet __ = rs;
                    EntityFactory<? extends Entity> factory = type.newFactory(rs)) {
                final Entity record = factory.newEntity();
                if (forceSingle && rs.next()) {
                    throw new IllegalArgumentException("Result not unique");
                }
                factory.complete();
                return record;
            }
        }
    }
}