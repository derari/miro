package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.result.*;
import org.cthul.miro.result.ResultBuilder;

/**
 * A query ready to be executed or submitted.
 * @param <Result> 
 */
public class SubmittableQuery<Result> {

    private final MiConnection cnn;
    private final MappedStatement<?> stmt;
    private final RB<Result, ?> ra;

    public <Entity> SubmittableQuery(MiConnection cnn, MappedStatement<Entity> stmt,
            ResultBuilder<Result, Entity> builder, EntityType<Entity> type) {
        this.cnn = cnn;
        this.stmt = stmt;
        this.ra = new RB<>(stmt, builder, type);
    }

    public Result execute(MiConnection cnn) throws SQLException {
        ResultSet rs = stmt.runQuery(cnn);
        return ra.build(rs, cnn);
    }

    public Result execute() throws SQLException {
        return execute(cnn);
    }

    public Result _execute(MiConnection cnn) {
        try {
            return execute(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Result _execute() {
        try {
            return execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MiFuture<Result> submit(final MiConnection cnn) throws SQLException {
        if (cnn == null) {
            throw new IllegalArgumentException("No connection given");
        }
        MiFuture<ResultSet> rs = stmt.submitQuery(cnn);
        return rs.onComplete(new MiFutureAction<MiFuture<ResultSet>, Result>() {
            @Override
            public Result call(MiFuture<ResultSet> result) throws Exception {
                return ra.build(result.get(), cnn);
            }
        });
    }

    public MiFuture<Result> submit() throws SQLException {
        return submit(cnn);
    }

    public MiFuture<Result> _submit(MiConnection cnn) {
        try {
            return submit(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MiFuture<Result> _submit() {
        try {
            return submit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class RB<R, E> {
        private final MappedStatement<E> stmt;
        private final ResultBuilder<R, E> builder;
        private final EntityType<E> type;

        public RB(MappedStatement<E> stmt, ResultBuilder<R, E> builder, EntityType<E> type) {
            this.stmt = stmt;
            this.builder = builder;
            this.type = type;
        }

        public R build(ResultSet rs, MiConnection cnn) throws SQLException {
            return builder.build(rs, type, stmt.getConfiguration(cnn));
        }
    }
}
