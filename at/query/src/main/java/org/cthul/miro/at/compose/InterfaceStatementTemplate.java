package org.cthul.miro.at.compose;

import java.lang.reflect.Method;
import org.cthul.miro.composer.template.Templates;
import org.cthul.miro.composer.ConfigureKey;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.composer.impl.AbstractTemplate;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.map.MappedStatementBuilder;

/**
 *
 * @param <Entity>
 * @param <Statement>
 */
public class InterfaceStatementTemplate<Entity, Statement extends SqlFilterableClause> 
                extends AbstractTemplate<MappedStatementBuilder<Entity, ? extends Statement>> {

    private final AnnotationReader<Entity, Statement> atReader = new AnnotationReader<Entity, Statement>(this::tryPut);

    public InterfaceStatementTemplate(Class<?> iface, Template<? super MappedStatementBuilder<Entity, ? extends Statement>> parent) {
        super(parent);
        atReader.readInterfaceClass(iface);
    }
    
    public InterfaceStatementTemplate(Template<? super MappedStatementBuilder<Entity, ? extends Statement>> parent) {
        super(parent);
    }

    @Override
    protected String getShortString() {
        Class<?> iface = atReader.getPrimaryInterface();
        if (iface != null) {
            return iface.getSimpleName();
        } else {
            return "Query-Interface";
        }
    }
    
    @Override
    protected Template<? super MappedStatementBuilder<Entity, ? extends Statement>> createPartTemplate(Object key) {
        if (!(key instanceof MethodKey)) return null;
        MethodKey mKey = (MethodKey) key;
        Method method = mKey.getMethod();
        ConfigureKey actualKey = atReader.readMethod(method);
        if (actualKey == null) {
            throw new IllegalArgumentException(
                    "No annotations found at " + method);
        }
        return Templates.link(actualKey);
    }
}
