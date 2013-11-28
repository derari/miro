package org.cthul.miro.query.adapter;

import java.util.List;

public interface QueryString<Builder extends QueryBuilder<? extends Builder>> extends QueryAdapter<Builder> {
    
    String getQueryString();
    
    int getBatchCount();
    
    List<Object> getArguments(int batch);
}
