package org.cthul.miro.map;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cthul.miro.cursor.ResultCursor;
import org.cthul.miro.cursor.ResultCursorBase;
import org.cthul.miro.map.ResultBuilder.EntityFactory;
import org.cthul.miro.map.ResultBuilder.ValueAdapter;

/**
 * Default implementations of {@link ResultBuilder}.
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

    public static class ListResult<Entity> implements ResultBuilder<List<Entity>, Entity> {

        @Override
        public List<Entity> adapt(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
            final List<Entity> result = new ArrayList<>();
            try (ValueAdapter<?> a1 = va; EntityFactory<?> a2 = ef; ResultSet a3 = rs) {
                ef.initialize(rs);
                va.initialize(rs);
                while (rs.next()) {
                    final Entity record = ef.newEntity();
                    va.apply(record);
                    result.add(record);
                }
                va.complete();
            }
            return result;
        }
    }

    public static <Entity> ArrayResult<Entity> getArrayResult(Class<Entity> clazz) {
        return new ArrayResult<>(clazz);
    }

    public static <Entity> ArrayResult<Entity> getArrayResult(Class<Entity> clazz, ResultBuilder<? extends Collection<? extends Entity>, Entity> listResult) {
        return new ArrayResult<>(clazz, listResult);
    }

    public static class ArrayResult<Entity> implements ResultBuilder<Entity[], Entity> {

        private final Class<Entity> entityClass;
        private final ResultBuilder<? extends Collection<? extends Entity>, Entity> listResult;

        public ArrayResult(Class<Entity> entityClass) {
            this(entityClass, getListResult(entityClass));
        }

        public ArrayResult(Class<Entity> entityClass, ResultBuilder<? extends Collection<? extends Entity>, Entity> listResult) {
            this.entityClass = entityClass;
            this.listResult = listResult;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entity[] adapt(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
            Collection<? extends Entity> result = listResult.adapt(rs, ef, va);
            return result.toArray((Entity[]) Array.newInstance(entityClass, result.size()));
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

    public static class CursorResult<Entity> implements ResultBuilder<ResultCursor<Entity>, Entity> {

        @Override
        public ResultCursor<Entity> adapt(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
            return new MappedResultCursor<>(rs, ef, va);
        }
    }

    protected static class MappedResultCursor<Entity> extends ResultCursorBase<Entity> {

        private final ResultSet rs;
        private final EntityFactory<Entity> ef;
        private final ValueAdapter<? super Entity> va;
        private boolean nextIsExpected = true;
        private boolean isAtNext = false;

        @SuppressWarnings("LeakingThisInConstructor")
        public MappedResultCursor(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
            this.rs = rs;
            this.ef = ef;
            this.va = va;
            ef.initialize(rs);
            va.initialize(rs);
            setCursorValue(ef.newCursorValue(this));
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
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Entity getFixCopy() {
            try {
                return ef.copy(cursor);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
            try {
                va.apply(cursor);
                va.complete();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            try (ValueAdapter<?> a1 = va; 
                 EntityFactory<?> a2 = ef; 
                 ResultSet a3 = rs) {
                // close all
            }
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

    public static class SingleResult<Entity> implements ResultBuilder<Entity, Entity> {

        private final boolean forceSingle;

        public SingleResult(boolean forceSingle) {
            this.forceSingle = forceSingle;
        }

        @Override
        public Entity adapt(ResultSet rs, EntityFactory<Entity> ef, ValueAdapter<? super Entity> va) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            try (ValueAdapter<?> a1 = va; EntityFactory<?> a2 = ef; ResultSet a3 = rs) {
                ef.initialize(rs);
                va.initialize(rs);
                final Entity record = ef.newEntity();
                va.apply(record);
                if (forceSingle && rs.next()) {
                    throw new IllegalArgumentException("Result not unique");
                }
                va.complete();
                return record;
            }
        }
    }
}
