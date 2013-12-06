package org.cthul.miro.query.adapter;

import org.cthul.miro.query.QueryType;

public interface DBAdapter {

    <Builder extends QueryBuilder<? extends Builder>> QueryAdapter<? extends Builder> newQueryAdapter(QueryType<Builder> queryType);
}
