package org.cthul.miro.at;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import org.cthul.miro.dml.IncludeMode;
import org.cthul.miro.dml.MappedDataQueryTemplateProvider;
import org.cthul.miro.graph.EntityGraphAdapter;
import org.cthul.miro.map.*;
import org.cthul.miro.query.InternalQueryBuilder;
import org.cthul.miro.query.template.*;

public class AnnotatedTemplateProvider<Entity> extends MappedDataQueryTemplateProvider<Entity> {
    
    private final AnnotationReader atData = new AnnotationReader(this);
    private final MappedTemplateProvider<Entity> parent;

    public AnnotatedTemplateProvider(Mapping<Entity> mapping) {
        super(mapping);
        this.parent = null;
    }
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AnnotatedTemplateProvider(Mapping<Entity> mapping, Class<?> iface) {
        this(mapping);
        addInterface(iface);
    }
    
    public AnnotatedTemplateProvider(MappedTemplateProvider<Entity> parent) {
        super(parent);
        this.parent = parent;
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AnnotatedTemplateProvider(MappedTemplateProvider<Entity> parent, Class<?> iface) {
        super(parent);
        this.parent = parent;
        addInterface(iface);
    }
    
    @Override
    protected void add(IncludeMode mode, Object key, QueryTemplatePart part) {
        super.add(mode, key, part);
        atData.keyAdded(key);
    }

    protected void addInterface(Class<?> iface) {
        atData.addInterface(iface);
    }
    
    ConcurrentMap<Method, AnnotatedTemplateProvider.InterfaceCall> getHandlers() {
        return atData.getHandlers();
    }
    
    @Override
    public Mapping<Entity> getMapping() {
        if (parent == null) return super.getMapping();
        return parent.getMapping();
    }

    @Override
    public EntityGraphAdapter<Entity> getGraphAdapter() {
        if (parent == null) return super.getGraphAdapter();
        return parent.getGraphAdapter();
    }

//    @Override
//    protected MappedTemplate customize(QueryTemplate template) {
//        return new AnnotatedTemplate(super.customize(template));
//    }
//    
    // visible to AnnotationReader
    @Override
    protected Using<?> using(Object... required) {
        return super.using(required);
    }

    @Override
    protected void table(String table) {
        super.table(table);
    }

    @Override
    protected void virtual(Object[] required, Object key) {
        super.virtual(required, key);
    }

    @Override
    protected void virtual(IncludeMode mode, Object[] required, Object key) {
        super.virtual(mode, required, key);
    }

    @Override
    protected Using<?> always(Object... requred) {
        return super.always(requred);
    }
    
    protected class AnnotatedTemplate extends AbstractQueryTemplate implements MappedTemplate<Entity> {

        private Mapping<Entity> mapping;
        
        public AnnotatedTemplate(MappedTemplate parent) {
            super(parent);
            this.mapping = parent.getMapping();
        }

        @Override
        public Mapping<Entity> getMapping() {
            return mapping;
        }

        @Override
        protected QueryTemplatePart autoPart(Object key) {
            QueryTemplatePart part = atData.getTemplateParts().get(key);
            if (part != null) {
                return part;
            }
            return super.autoPart(key);
        }
    }
    
    public static interface InterfaceCall {
        Object call(AnnotatedQueryHandler<?,?> handler, InternalQueryBuilder builder, Object[] args) throws Throwable;
    }
}
