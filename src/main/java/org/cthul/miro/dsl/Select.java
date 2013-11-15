package org.cthul.miro.dsl;

import java.util.Arrays;
import org.cthul.miro.MiConnection;
import org.cthul.miro.map.MappedQueryString;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.SimpleMapping;

public class Select {

    private static final Select SELECT_ALL = new Select();

    public static Select select() {
        return SELECT_ALL;
    }

    public static Select select(String... fields) {
        return new Select(fields);
    }
    private final MiConnection cnn;
    private final String[] fields;

    public Select() {
        this(null, (String[]) null);
    }

    public Select(MiConnection cnn) {
        this(cnn, (String[]) null);
    }

    public Select(String... fields) {
        this(null, fields);
    }

    public Select(MiConnection cnn, String... fields) {
        this.cnn = cnn;
        this.fields = fields;
    }

    public <Qry> Qry from(View<Qry> view) {
        return view.select(cnn, fields);
    }
    
    public <T> MappedStatement<T> fromQuery(SimpleMapping<T> mapping, String query, Object... args) {
        String[] select = fields;
        if (select.length == 0 || 
                (select.length == 1 && select[0].equals("*"))) {
            return new MappedQueryString<>(cnn, mapping, null, query, args);
        } else {
            return new MappedQueryString<>(cnn, mapping, Arrays.asList(select), query, args);
        }
    }
}
