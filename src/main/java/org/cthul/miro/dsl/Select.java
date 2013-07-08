package org.cthul.miro.dsl;

import org.cthul.miro.MiConnection;

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
}
