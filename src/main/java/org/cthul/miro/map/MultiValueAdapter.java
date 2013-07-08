package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.cthul.miro.map.ResultBuilder.ValueAdapter;

public class MultiValueAdapter<T> implements ValueAdapter<T> {

    @SuppressWarnings("unchecked")
    public static <T> ValueAdapter<T> join(List<ValueAdapter<? super T>> pp) {
        return new MultiValueAdapter<>(pp.toArray(new ValueAdapter[pp.size()]));
    }
    
    private final ValueAdapter<T>[] pp;

    public MultiValueAdapter(ValueAdapter<T>[] pp) {
        this.pp = pp;
    }

    @Override
    public void initialize(ResultSet rs) throws SQLException {
        for (int i = 0; i < pp.length; i++) {
            pp[i].initialize(rs);
        }
    }

    @Override
    public void apply(T record) throws SQLException {
        for (int i = 0; i < pp.length; i++) {
            pp[i].apply(record);
        }
    }

    @Override
    public void complete() throws SQLException {
        for (int i = 0; i < pp.length; i++) {
            pp[i].complete();
        }
    }

    @Override
    public void close() throws SQLException {
        for (int i = 0; i < pp.length; i++) {
            pp[i].close();
        }
    }
}
