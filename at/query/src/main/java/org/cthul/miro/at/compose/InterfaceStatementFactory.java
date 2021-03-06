package org.cthul.miro.at.compose;

import java.util.List;
import org.cthul.miro.composer.RequestComposer;
import org.cthul.miro.composer.StatementFactory;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.map.MappedStatementBuilder;

/**
 *
 */
public class InterfaceStatementFactory<Entity, Builder, Req> implements StatementFactory<Builder, Req> {
    
    private final StatementFactory<Builder, RequestComposer<?>> factory;
    private final Class<Req> stmtType;

    public InterfaceStatementFactory(StatementFactory<Builder, RequestComposer<?>> factory, Class<Req> stmtType) {
        this.factory = factory;
        this.stmtType = stmtType;
    }

    @Override
    public Req create(Template<? super Builder> template, List<?> attributes) {
        RequestComposer<?> c = factory.create(template, attributes);
        return InterfaceHandler.create(stmtType, c, c);
    }
}
