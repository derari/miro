package org.cthul.miro.map;

import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.result.ResultBuilder;
import org.cthul.miro.result.Results;
import org.cthul.miro.view.ViewR;

public class MappedQueryStringView<Result> implements ViewR<MappedQueryString<Result>> {

    private final String query;
    private final Mapping<?> mapping;
    private final ResultBuilder<Result, ?> resultBuilder;
    private final Object[] args;
    private final List<Object> configs = new ArrayList<>();

    public MappedQueryStringView(Mapping<?> mapping, ResultBuilder<Result, ?> resultBuilder, String query, Object... args) {
        this.query = query;
        this.mapping = mapping;
        this.resultBuilder = resultBuilder;
        this.args = args;
    }

    public MappedQueryStringView(Mapping<?> mapping, String query, Object... args) {
        this.query = query;
        this.mapping = mapping;
        this.resultBuilder = (ResultBuilder<Result, ?>) Results.getBuilder();
        this.args = args;
    }

    @Override
    public MappedQueryString<Result> select() {
        MappedQueryString<Result> qry = new MappedQueryString<>(mapping, resultBuilder, query);
        if (args != null && args.length > 0) {
            qry.batch(args);
        }
        for (Object o: configs) {
            qry.configure(o);
        }
        return qry;
    }

    @Override
    public MappedQueryString<Result> select(String... attributes) {
        return select().select(attributes);
    }
    
    public MappedQueryStringView<Result> withArgs(Object... args) {
        return new MappedQueryStringView<>(mapping, resultBuilder, query, args);
    }
    
    public MappedQueryStringView<Result> configure(Object o) {
        configs.add(o);
        return this;
    }
}
