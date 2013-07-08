package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.map.ResultBuilder.EntityFactory;

/**
 * A query ready to be executed or submitted.
 * @param <R> 
 */
public class SubmittableQuery<R> {

    private final MiConnection cnn;
    private final MappedStatement<?> stmt;
    private final RA<R, ?> ra;

    public <E> SubmittableQuery(MiConnection cnn, MappedStatement<E> stmt,
            ResultBuilder<R, E> ra, EntityFactory<E> ef) {
        this.cnn = cnn;
        this.stmt = stmt;
        this.ra = new RA<>(stmt, ra, ef);
    }

    public R execute(MiConnection cnn) throws SQLException {
        ResultSet rs = stmt.runQuery(cnn);
        return ra.adapt(rs, cnn);
    }

    public R execute() throws SQLException {
        return execute(cnn);
    }

    public R _execute(MiConnection cnn) {
        try {
            return execute(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public R _execute() {
        try {
            return execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MiFuture<R> submit(final MiConnection cnn) throws SQLException {
        MiFuture<ResultSet> rs = stmt.submitQuery(cnn);
        return rs.onComplete(new MiFutureAction<MiFuture<ResultSet>, R>() {
            @Override
            public R call(MiFuture<ResultSet> result) throws Exception {
                return ra.adapt(result.get(), cnn);
            }
        });
    }

    public MiFuture<R> submit() throws SQLException {
        return submit(cnn);
    }

    public MiFuture<R> _submit(MiConnection cnn) {
        try {
            return submit(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MiFuture<R> _submit() {
        try {
            return submit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class RA<R, E> {

        private final MappedStatement<E> stmt;
        private final ResultBuilder<R, E> rb;
        private final EntityFactory<E> ef;

        public RA(MappedStatement<E> stmt, ResultBuilder<R, E> ra, EntityFactory<E> ef) {
            this.stmt = stmt;
            this.rb = ra;
            this.ef = ef;
        }

        public R adapt(ResultSet rs, MiConnection cnn) throws SQLException {
            return rb.adapt(rs, ef, stmt.buildValueAdapter(cnn));
        }
    }
}
