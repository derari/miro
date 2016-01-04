package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Key;

public interface SqlTemplatesBuilder<This extends SqlTemplatesBuilder<This>> {
    
    default This mainTable(String table) {
        MiSqlParser.Table t = MiSqlParser.parseFromPart(table);
        return mainTable(t, t.getKey());
    }
    
    This mainTable(QlCode code, String key);
    
    default This attribute(String id, boolean key, String attribute) {
        return attribute(ResultScope.DEFAULT, key, id, attribute);
    }
    
    default This attribute(String id, boolean key, MiSqlParser.Attribute attribute) {
        return attribute(ResultScope.DEFAULT, key, id, attribute);
    }
    
    default This attribute(ResultScope scope, boolean key, String id, String attribute) {
        return attribute(scope, key, id, MiSqlParser.parseAttributePart(attribute));
    }
    
    This attribute(ResultScope scope, boolean key, String id, MiSqlParser.Attribute attribute);
    
    <V extends Template<? super SqlFilterableClause>> This where(Key<? super V> key, V filter);
}
