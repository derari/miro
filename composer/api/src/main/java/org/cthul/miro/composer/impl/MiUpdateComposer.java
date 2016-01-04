package org.cthul.miro.composer.impl;

import java.util.List;
import org.cthul.miro.composer.StatementFactory;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.stmt.MiUpdate;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.futures.MiAction;

/**
 *
 */
public class MiUpdateComposer extends AbstractRequestComposer<MiUpdateString, MiUpdateString> 
                              implements MiUpdate {

    public MiUpdateComposer(Template<? super MiUpdateString> template) {
        super(MiUpdateString.TYPE, template);
    }
    
    public MiUpdateComposer(Template<? super MiUpdateString> template, List<?> attributes) {
        this(template);
        requireAll(attributes);
    }

    @Override
    protected MiUpdateString newBuilder(MiUpdateString statement) {
        return statement;
    }

    @Override
    public Long execute() throws MiException {
        return buildStatement().execute();
    }

    @Override
    public MiAction<Long> asAction() {
        return buildStatement().asAction();
    }
    
    public static StatementFactory<MiUpdateString, MiUpdateComposer> factory() {
        return MiUpdateComposer::new;
    }
}
