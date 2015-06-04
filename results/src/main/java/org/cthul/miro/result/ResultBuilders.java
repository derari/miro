package org.cthul.miro.result;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.entity.EntityFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cthul.miro.result.cursor.ResultCursor;
import org.cthul.miro.result.cursor.ResultCursorBase;

/**
 * Default implementations of {@link EntityResultBuilder}.
 */
public class ResultBuilders {

    protected ResultBuilders() {
    }
 
    @SuppressWarnings("rawtypes")
    private static ListResult LIST_RESULT = null;

    @SuppressWarnings("unchecked")
    public static <Entity> ListResult<Entity> getListResult() {
        if (LIST_RESULT == null) {
            LIST_RESULT = new ListResult<>();
        }
        return LIST_RESULT;
    }

    public static <Entity> ListResult<Entity> getListResult(Class<Entity> clazz) {
        return getListResult();
    }

    public static class ListResult<Entity> implements EntityResultBuilder<List<Entity>, Entity> {

        @Override
        public List<Entity> build(MiResultSet rs, EntityType<Entity> type) throws MiException {
            final List<Entity> result = new ArrayList<>();
            try (MiResultSet __ = rs;
                    EntityFactory<Entity> factory = type.newFactory(rs)) {
                while (rs.next()) {
                    final Entity record = factory.newEntity();
                    result.add(record);
                }
                factory.complete();
            }
            return result;
        }
    }
    
    private static ArrayResult ARRAY_RESULT = null;

    public static <Entity> ArrayResult<Entity> getArrayResult() {
        if (ARRAY_RESULT == null) {
            ARRAY_RESULT = new ArrayResult<>(getListResult());
        }
        return ARRAY_RESULT;
    }
    
    public static <Entity> ArrayResult<Entity> getArrayResult(Class<Entity> clazz) {
        return getArrayResult();
    }

    public static <Entity> ArrayResult<Entity> getArrayResult(EntityResultBuilder<? extends Collection<? extends Entity>, Entity> listResult) {
        return new ArrayResult<>(listResult);
    }

    public static class ArrayResult<Entity> implements EntityResultBuilder<Entity[], Entity> {

        private final EntityResultBuilder<? extends Collection<? extends Entity>, Entity> listResult;

        public ArrayResult(EntityResultBuilder<? extends Collection<? extends Entity>, Entity> listResult) {
            this.listResult = listResult;
        }

        @Override
        public Entity[] build(MiResultSet rs, EntityType<Entity> type) throws MiException {
            Collection<? extends Entity> result = listResult.build(rs, type);
            return result.toArray(type.newArray(result.size()));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static CursorResult CURSOR_RESULT = null;

    @SuppressWarnings("unchecked")
    public static <Entity> CursorResult<Entity> getCursorResult() {
        if (CURSOR_RESULT == null) {
            CURSOR_RESULT = new CursorResult<>();
        }
        return CURSOR_RESULT;
    }

    public static <Entity> CursorResult<Entity> getCursorResult(Class<Entity> clazz) {
        return getCursorResult();
    }

    public static class CursorResult<Entity> implements EntityResultBuilder<ResultCursor<Entity>, Entity> {

        @Override
        public ResultCursor<Entity> build(MiResultSet rs, EntityType<Entity> type) throws MiException {
            return new MappedResultCursor<>(rs, type);
        }
    }

    protected static class MappedResultCursor<Entity> extends ResultCursorBase<Entity> {

        private final MiResultSet rs;
        private final EntityFactory<Entity> factory;
        private boolean nextIsExpected = true;
        private boolean isAtNext = false;

        @SuppressWarnings("LeakingThisInConstructor")
        public MappedResultCursor(MiResultSet rs, EntityType<Entity> type) throws MiException {
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
    
    @SuppressWarnings("rawtypes")
    private static SingleResult SINGLE_RESULT = null;

    @SuppressWarnings("unchecked")
    public static <Entity> SingleResult<Entity> getSingleResult() {
        if (SINGLE_RESULT == null) {
            SINGLE_RESULT = new SingleResult<>(true);
        }
        return SINGLE_RESULT;
    }

    public static <Entity> SingleResult<Entity> getSingleResult(Class<Entity> clazz) {
        return getSingleResult();
    }
    
    @SuppressWarnings("rawtypes")
    private static SingleResult FIRST_RESULT = null;

    @SuppressWarnings("unchecked")
    public static <Entity> SingleResult<Entity> getFirstResult() {
        if (FIRST_RESULT == null) {
            FIRST_RESULT = new SingleResult<>(false);
        }
        return FIRST_RESULT;
    }

    public static <Entity> SingleResult<Entity> getFirstResult(Class<Entity> clazz) {
        return getFirstResult();
    }

    public static class SingleResult<Entity> implements EntityResultBuilder<Entity, Entity> {

        private final boolean forceSingle;

        public SingleResult(boolean forceSingle) {
            this.forceSingle = forceSingle;
        }

        @Override
        public Entity build(MiResultSet rs, EntityType<Entity> type) throws MiException {
            if (!rs.next()) {
                return null;
            }
            try (MiResultSet __ = rs;
                    EntityFactory<Entity> factory = type.newFactory(rs)) {
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