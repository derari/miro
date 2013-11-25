package org.cthul.miro.query.parts;

import java.util.List;
import org.cthul.miro.query.api.QueryPart;

public interface FilterValueQueryPart extends QueryPart {

    void selectFilterValue(String key);
    
    void appendFilterValuesTo(List<Object> args);
}
