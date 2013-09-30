package org.cthul.miro.at;

import java.lang.reflect.Proxy;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dsl.View;
import org.cthul.miro.dsl.ViewBase;
import org.cthul.miro.map.Mapping;

/**
 *
 */
public class AnnotatedView<Qry, Entity> extends ViewBase<Qry> {
    
    private final Class<Qry> queryInterface;
    private final Mapping<Entity> mapping;
    private final AnnotatedQueryTemplate<Entity> template;

    public AnnotatedView(Class<Qry> queryInterface, Mapping<Entity> mapping, AnnotatedQueryTemplate<Entity> template) {
        this.queryInterface = queryInterface;
        this.mapping = mapping;
        this.template = template;
        template.addInterface(queryInterface);
    }
    
    public AnnotatedView(Class<Qry> queryInterface, Mapping<Entity> mapping) {
        this(queryInterface, mapping, new AnnotatedQueryTemplate<>(mapping));
    }
    
    public AnnotatedView(Class<Qry> queryInterface, AnnotatedQueryTemplate<Entity> template) {
        this(queryInterface, template.getMapping(), template);
    }

    @Override
    public Qry select(MiConnection cnn, String... fields) {
        View thisView = this;
        AnnotatedQueryHandler<Entity> handler = new AnnotatedQueryHandler<>(cnn, mapping, template, thisView, fields);
        return (Qry) Proxy.newProxyInstance(queryInterface.getClassLoader(), new Class[]{queryInterface}, handler);
    }
    
}
