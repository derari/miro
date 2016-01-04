package org.cthul.miro.at.compose;

import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.view.composer.SimpleCRUDTemplateLayer;

/**
 *
 * @param <Entity>
 * @param <CS>
 * @param <RS>
 * @param <US>
 * @param <DS>
 */
public class InterfaceTemplateLayer<Entity, 
                        CS extends SqlFilterableClause, 
                        RS extends SqlFilterableClause, 
                        US extends SqlFilterableClause, 
                        DS extends SqlFilterableClause>
                implements SimpleCRUDTemplateLayer<
                            MappedStatementBuilder<Entity, ? extends CS>,
                            MappedStatementBuilder<Entity, ? extends RS>,
                            MappedStatementBuilder<Entity, ? extends US>,
                            MappedStatementBuilder<Entity, ? extends DS>> {
    
    protected <S extends SqlFilterableClause> Template<? super MappedStatementBuilder<Entity, ? extends S>> template(Template<? super MappedStatementBuilder<Entity, ? extends S>> template) {
        return new InterfaceStatementTemplate<>(template);
    }

    @Override
    public Template<? super MappedStatementBuilder<Entity, ? extends CS>> insertTemplate(Template<? super MappedStatementBuilder<Entity, ? extends CS>> template) {
        return template(template);
    }

    @Override
    public Template<? super MappedStatementBuilder<Entity, ? extends RS>> selectTemplate(Template<? super MappedStatementBuilder<Entity, ? extends RS>> template) {
        return template(template);
    }

    @Override
    public Template<? super MappedStatementBuilder<Entity, ? extends US>> updateTemplate(Template<? super MappedStatementBuilder<Entity, ? extends US>> template) {
        return template(template);
    }

    @Override
    public Template<? super MappedStatementBuilder<Entity, ? extends DS>> deleteTemplate(Template<? super MappedStatementBuilder<Entity, ? extends DS>> template) {
        return template(template);
    }
    
    public static <Entity, 
            CS extends SqlFilterableClause, 
            RS extends SqlFilterableClause, 
            US extends SqlFilterableClause, 
            DS extends SqlFilterableClause> InterfaceTemplateLayer<Entity, CS, RS, US, DS> get() {
        return INSTANCE;
    }
    
    private static final InterfaceTemplateLayer INSTANCE = new InterfaceTemplateLayer();
}
