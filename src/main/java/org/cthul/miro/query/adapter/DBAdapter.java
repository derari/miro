package org.cthul.miro.query.adapter;

import org.cthul.miro.query.api.QueryType;

public interface DBAdapter {

    <Builder extends QueryBuilder<? extends Builder>> QueryAdapter<? extends Builder> newQueryAdapter(QueryType<Builder> queryType);
}
