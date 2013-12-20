package org.cthul.miro.dml;

import java.sql.SQLException;
import org.cthul.miro.MiConnection;

public interface MappedCUD<Entity> {
    
    MappedCUD values(Entity... args);
    
    public void execute(MiConnection cnn) throws SQLException;
    
//    public MappedCUD(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider, Object[] select) {
//        super(type, templateProvider);
//        put(DataQueryKey.ATTRIBUTE, select);
//        if (type == DataQuery.INSERT) {
//            put(DataQueryKey.ADD_GENERATED_KEYS_TO_RESULT);
//        }
//    }
//    
//    public MappedCUD values(Entity... args) {
//        addToGraph(args);
//        put2(MappedDataQueryKey.ENTITIES, DataQuerySubkey.ADD_ALL, args);
//        return this;
//    }
//    
//    public void execute(MiConnection cnn) throws SQLException {
//        ResultSet rs = executeJdbc(cnn);
//        if (rs != null) {
//            ResultBuilders.<Entity>getListResult()
//                    .build(rs, getEntityType(), getConfiguration(cnn));
//        }
//    }
//
//    @Override
//    protected EntityType<Entity> typeForEntities(List<Entity> entities) {
////        if (getQueryType() == DataQuery.INSERT) {
//            return entitiesInOrder(entities);
////        }
////        return graphTypeForEntities(entities);
//    }
}
