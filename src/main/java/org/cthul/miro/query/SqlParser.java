package org.cthul.miro.query;

import org.cthul.miro.doc.AutoDependencies;
import org.cthul.miro.doc.MultiValue;
import org.cthul.miro.doc.AutoKey;

public interface SqlParser<This extends SqlParser<This>> {

    This select(@MultiValue @AutoKey @AutoDependencies String selectClause);
    
    This select(@MultiValue @AutoKey @AutoDependencies String... selectClause);
    
    This table(@AutoKey String table);
    
    This attributes(String... attributes);
    
    This from(@AutoKey String fromClause);
    
    This from(@AutoKey String... fromClause);
    
    This join(@AutoKey String joinClause);
    
    This join(@AutoKey String... joinClause);
    
    This where(String key, String whereClause);
    
    This where(String whereClause);
//    
//    This groupBy(String key, String whereClause);
//    
//    This groupBy(String whereClause);
//    
//    This having(String key, String havingClause);
//    
//    This having(String havingClause);
//    
//    This orderBy(String key, String orderByClause);
//    
//    This orderBy(String orderByClause);
}
