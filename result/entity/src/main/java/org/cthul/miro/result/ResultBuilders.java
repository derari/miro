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
        public List<Entity> build(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
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
    
    public static <Entity> ArrayResult<Entity> getArrayResult() {
        return ArrayResult.ARRAY_RESULT;
    }
    
    public static <Entity> ArrayResult<Entity> getArrayResult(Class<Entity> clazz) {
        return getArrayResult();
    }

    public static <Entity> ArrayResult<Entity> getArrayResult(EntityResultBuilder<? extends Collection<Entity>, Entity> listResult) {
        return new ArrayResult<>(listResult);
    }

    public static class ArrayResult<Entity> implements EntityResultBuilder<Entity[], Entity> {
        
        private static final ArrayResult ARRAY_RESULT = new ArrayResult<>(getListResult());

        private final EntityResultBuilder<? extends Collection<Entity>, Entity> listResult;

        public ArrayResult(EntityResultBuilder<? extends Collection<Entity>, Entity> listResult) {
            this.listResult = listResult;
        }

        @Override
        public Entity[] build(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
            Collection<Entity> result = listResult.build(rs, type);
            return result.toArray(type.newArray(result.size()));
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
        public ResultCursor<Entity> build(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
            return new MappedResultCursor<>(rs, type);
        }
    }

    protected static class MappedResultCursor<Entity> extends ResultCursorBase<Entity> {

        private final MiResultSet rs;
        private final EntityFactory<? extends Entity> factory;
        private boolean nextIsExpected = true;
        private boolean isAtNext = false;

        @SuppressWarnings("LeakingThisInConstructor")
        public MappedResultCursor(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
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
        private static final SingleResult FIRST_RESULT = new SingleResult<>(false);

        private final boolean forceSingle;

        public SingleResult(boolean forceSingle) {
            this.forceSingle = forceSingle;
        }

        @Override
        public Entity build(MiResultSet rs, EntityType<? extends Entity> type) throws MiException {
            if (!rs.next()) {
                return null;
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