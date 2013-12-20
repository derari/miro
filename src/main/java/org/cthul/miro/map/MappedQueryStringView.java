package org.cthul.miro.map;

import org.cthul.miro.result.ResultBuilder;
import org.cthul.miro.result.Results;
import org.cthul.miro.view.ViewR;

public class MappedQueryStringView<Result> implements ViewR<MappedQueryString<Result>> {

    private final String query;
    private final Mapping<?> mapping;
    private final org.cthul.miro.result.ResultBuilder<Result, ?> resultBuilder;

    public MappedQueryStringView(String query, Mapping<?> mapping, ResultBuilder<Result, ?> resultBuilder) {
        this.query = query;
        this.mapping = mapping;
        this.resultBuilder = resultBuilder;
    }

    public MappedQueryStringView(String query, Mapping<?> mapping) {
        this.query = query;
        this.mapping = mapping;
        this.resultBuilder = (ResultBuilder<Result, ?>) Results.getBuilder();
    }

    @Override
    public MappedQueryString<Result> select() {
        return new MappedQueryString<>(mapping, resultBuilder, query);
    }

    @Override
    public MappedQueryString<Result> select(String... attributes) {
        return select().select(attributes);
    }
}
