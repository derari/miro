package org.cthul.miro.query.parts;

import java.util.List;

public class VirtualQueryPart extends AbstractQueryPart {

    public VirtualQueryPart(String key) {
        super(key);
    }

    @Override
    public void appendSqlTo(StringBuilder sqlBuilder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendArgsTo(List<Object> args) {
        throw new UnsupportedOperationException();
    }
}
