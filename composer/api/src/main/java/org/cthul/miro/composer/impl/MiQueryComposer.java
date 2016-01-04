package org.cthul.miro.composer.impl;

import java.util.List;
import org.cthul.miro.composer.StatementFactory;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.stmt.MiQuery;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class MiQueryComposer extends AbstractRequestComposer<MiQueryString, MiQueryString> 
                             implements MiQuery {

    public MiQueryComposer(Template<? super MiQueryString> template) {
        super(MiQueryString.TYPE, template);
    }

    private MiQueryComposer(Template<? super MiQueryString> template, List<?> attributes) {
        this(template);
        requireAll(attributes);
    }

    @Override
    protected MiQueryString newBuilder(MiQueryString statement) {
        return statement;
    }

    @Override
    public MiResultSet execute() throws MiException {
        return buildStatement().execute();
    }

    @Override
    public MiAction<MiResultSet> asAction() {
        return buildStatement().asAction();
    }
    
    public static StatementFactory<MiQueryString, MiQueryComposer> factory() {
        return MiQueryComposer::new;
    }
}
