package org.cthul.miro.view.msql;

import java.util.function.Function;
import org.cthul.miro.at.compose.InterfaceTemplateLayer;
import org.cthul.miro.at.compose.MappedInterfaceStatementBuilder;
import org.cthul.miro.at.model.EntitySchemaBuilder;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.composer.StatementFactory;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.MiException;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.sql.MappedEntityType;
import org.cthul.miro.map.sql.MappedSqlTemplates;
import org.cthul.miro.map.sql.MappedSqlTemplatesBuilder;
import org.cthul.miro.util.Closables;
import org.cthul.miro.view.ViewCRUD;
import org.cthul.miro.view.composer.CRUDStatementFactory;

/**
 *
 */
public class MappedSqlViewBuilder<Entity, C, R, U, D> {
    
//    private final GraphSchema schema;
//    private final Class<Entity> entityClass;
    private final MappedSqlTemplatesBuilder<Entity> templateBuilder;
    private final MappedInterfaceStatementBuilder<Entity> interfaceStatementBuilder;
    private StatementFactory<? extends MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, R> selectFactory;

    public MappedSqlViewBuilder(Class<Entity> entityClass) {
        this(new EntitySchemaBuilder(), entityClass);
    }
            
    public MappedSqlViewBuilder(GraphSchema schema, Class<Entity> entityClass) {
//        this.schema = schema;
//        this.entityClass = entityClass;
        try {
            EntityType<Entity> et = schema.newFakeGraph(null).entityType(entityClass);
            interfaceStatementBuilder = new MappedInterfaceStatementBuilder<>(entityClass, et);
            if (et instanceof MappedEntityType) {
                templateBuilder = ((MappedEntityType<Entity>) et).getMappingBuilder();
            } else {
                templateBuilder = new MappedSqlTemplates<>();
            }
        } catch (MiException e) {
            throw Closables.unchecked(e);
        }
    }
    
    public ViewCRUD<C, R, U, D> build() {
        return new MappedSqlView<>(
                    new CRUDStatementFactory<>(null, selectFactory, null, null),
                    templateBuilder.asTemplates()
                        .push(InterfaceTemplateLayer.get()));
    }
    
    public <R1> MappedSqlViewBuilder<Entity, C, R1, U, D> select(Class<R1> queryType) {
        return select(interfaceStatementBuilder.select(queryType));
    }
    
    public <R1 extends Composer> MappedSqlViewBuilder<Entity, C, R1, U, D> select(Function<? super Template<? super MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>>, R1> factory) {
        return select((template, attributes) -> {
            R1 c = factory.apply(template);
            c.requireAll(attributes);
            return c;
        });
    }
    
    public <R1> MappedSqlViewBuilder<Entity, C, R1, U, D> select(StatementFactory<? extends MappedStatementBuilder<Entity, ? extends SelectQueryBuilder>, R1> factory) {
        MappedSqlViewBuilder<Entity, C, R1, U, D> self = (MappedSqlViewBuilder) this;
        self.selectFactory = factory;
        return self;
    }
    
    public <C1, R1, U1, D1> MappedSqlViewBuilder<Entity, C1, R1, U1, D1> view(Class<? super ViewCRUD<C1, R1, U1, D1>> viewType) {
        return (MappedSqlViewBuilder) this;
    }
}
