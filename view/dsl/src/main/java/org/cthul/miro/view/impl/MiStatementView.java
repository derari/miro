package org.cthul.miro.view.impl;

import org.cthul.miro.composer.impl.MiQueryComposer;
import org.cthul.miro.composer.impl.MiUpdateComposer;
import org.cthul.miro.db.stmt.MiQueryString;
import org.cthul.miro.db.stmt.MiUpdateString;
import org.cthul.miro.view.composer.CRUDStatementFactory;
import org.cthul.miro.view.composer.CRUDTemplates;

/**
 *
 */
public class MiStatementView extends SimpleCrudView
        <MiUpdateString, MiQueryString, MiUpdateString, MiUpdateString, 
         MiUpdateComposer, MiQueryComposer, MiUpdateComposer, MiUpdateComposer> {
    
    private static final CRUDStatementFactory<MiUpdateString, MiQueryString, MiUpdateString, MiUpdateString, MiUpdateComposer, MiQueryComposer, MiUpdateComposer, MiUpdateComposer> FACTORY = new CRUDStatementFactory<>()
            .withInsert(MiUpdateComposer.factory())
            .withSelect(MiQueryComposer.factory())
            .withUpdate(MiUpdateComposer.factory())
            .withDelete(MiUpdateComposer.factory());

    public MiStatementView(CRUDStatementFactory<MiUpdateString, MiQueryString, MiUpdateString, MiUpdateString, MiUpdateComposer, MiQueryComposer, MiUpdateComposer, MiUpdateComposer> factory, CRUDTemplates<? super MiUpdateString, ? super MiQueryString, ? super MiUpdateString, ? super MiUpdateString> templates) {
        super(factory, templates);
    }

    public MiStatementView(CRUDTemplates<? super MiUpdateString, ? super MiQueryString, ? super MiUpdateString, ? super MiUpdateString> templates) {
        this(FACTORY, templates);
    }

    public MiStatementView() {
        this(NoTemplates.getInstance());
    }
}
