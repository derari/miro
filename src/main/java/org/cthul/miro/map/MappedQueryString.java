package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cthul.miro.*;
import org.cthul.miro.query.QueryType;
import org.cthul.miro.query.adapter.*;
import org.cthul.miro.query.sql.*;
import org.cthul.miro.result.*;
import org.cthul.miro.result.ResultBuilder;
import org.cthul.miro.result.Results;

public class MappedQueryString<Result> implements QueryWithResult<Result> {
    
    private final String string;
    private final Mapping<?> mapping;
    private final ResultBuilder<Result, ?> resultBuilder;
    private final List<Object[]> batches = new ArrayList<>();
    private final List<String> selected = new ArrayList<>();
    private List<ConfigurationProvider<?>> configs = null;
    private List<Object[]> configArgs = null;

    public MappedQueryString(Mapping<?> mapping, ResultBuilder<Result, ?> resultBuilder, String string) {
        this.string = string;
        this.mapping = mapping;
        this.resultBuilder = resultBuilder;
    }

    public MappedQueryString(Mapping<?> mapping, String query) {
        this.string = query;
        this.mapping = mapping;
        this.resultBuilder = (ResultBuilder) Results.getBuilder();
    }
    
    public QueryType<?> getQueryType() {
        return BasicQuery.STRING;
    }

    public MappedQueryString<Result> select(String... fields) {
        selected.addAll(Arrays.asList(fields));
        return this;
    }
    
    public MappedQueryString<Result> batch(Object... args) {
        batches.add(args);
        return this;
    }
    
    public MappedQueryString<Result> configure(Object config, Object... args) {
        if (configs == null) {
            configs = new ArrayList<>();
            configArgs = new ArrayList<>();
        }
        configs.add(ConfigurationInstance.asProvider(config));
        configArgs.add(args);
        return this;
    }
    
    public MappedQueryString<Result> configure(Object config) {
        return configure(config, (Object[]) null);
    }
    
    public MappedQueryString<Result> configureAll(Object... config) {
        for (Object o: config) {
            configure(o);
        }
        return this;
    }
    
    @Override
    public Result execute(MiConnection cnn) throws SQLException {
        ResultSet rs = executeJdbc(cnn);
        if (rs != null) {
            ResultBuilder rb = resultBuilder;
            return (Result) rb.build(rs, getEntityType(), getConfiguration(cnn));
        }
        return null;
    }

    @Override
    public Result _execute(MiConnection cnn) {
        try {
            return execute(cnn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MiFuture<Result> submit(final MiConnection cnn) {
        return cnn.submit(null, new MiFutureAction<Void, Result>() {
                  @Override
                  public Result call(Void arg) throws Exception {
                      return execute(cnn);
                  }
              });
    }
    
    protected ResultSet executeJdbc(MiConnection cnn) throws SQLException {
        JdbcQuery<?> query = getAdapter(cnn.getJdbcAdapter());
        return cnn.execute(query);
    }
    
    protected MiFuture<ResultSet> submitJdbc(final MiConnection cnn) {
        MiFutureAction<MappedQueryString<?>, ResultSet> exec = new MiFutureAction<MappedQueryString<?>, ResultSet>() {
            @Override
            public ResultSet call(MappedQueryString<?> arg) throws Exception {
                return arg.executeJdbc(cnn);
            }
        };
        return cnn.submit(this, exec);
    }
    
    protected <T extends QueryAdapter<?>> T getAdapter(DBAdapter dbAdapter) {
        QueryAdapter<?> a = dbAdapter.newQueryAdapter((QueryType) getQueryType());
        StringQueryBuilder<?> b = (StringQueryBuilder) a.getBuilder();
        b.query(string);
        for (Object[] batch: batches) {
            b.batch(batch);
        }
        return (T) a;
    }
    
    protected EntityType<?> getEntityType() {
        return mapping;
    }

    public List<String> getSelected() {
        return selected;
    }
    
    protected EntityConfiguration<?> getFieldsConfiguration(MiConnection cnn) {
        List<String> result = getSelected();
        if (result.isEmpty()) {
            return mapping.newFieldConfiguration(Arrays.asList("*"));
        }
        return mapping.newFieldConfiguration(result);
    }
    
    protected EntityConfiguration<?> getConfiguration(MiConnection cnn) {
        if (configs == null) return getFieldsConfiguration(cnn);
        List<EntityConfiguration<?>> result = new ArrayList<>();
        result.add(getFieldsConfiguration(cnn));
        int len = configs.size();
        for (int i = 0; i < len; i++) {
            ConfigurationProvider cp = configs.get(i);
            result.add(cp.getConfiguration(cnn, mapping, configArgs.get(i)));
        }
        return CombinedEntityConfig.combine((List) result);
    }
}
