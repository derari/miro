package org.cthul.miro.composer.sql;

import java.util.HashMap;
import java.util.Map;
import org.cthul.miro.composer.QueryParts;
import org.cthul.miro.composer.template.AbstractTemplate;
import org.cthul.miro.composer.template.QueryPartType;
import org.cthul.miro.db.sql.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public class SqlTemplateBuilder {

    private final Map<String, Attribute> attributes = new HashMap<>();
    private final Map<Object, QueryPartType<?>> templates = new HashMap<>();
    private QlCode mainTable;

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }
    
    public void from(String from) {
        MiSqlParser.Table table = MiSqlParser.parseFromPart(from);
        mainTable = table;
        templates.put(table.getKey(), QueryParts.proxy(SqlQueryKey.TABLE));
    }
    
    public void attribute(String id, String attribute) {
        MiSqlParser.Attribute sqlAt = MiSqlParser.parseAttributePart(attribute);
        Attribute at = Attribute.forColumn(id, sqlAt.getTable(), sqlAt.getColumn(), sqlAt.getName());
        attributes.put(id, at);
        templates.put(id, QueryParts.put(SqlQueryKey.ATTRIBUTE, at));
    }
    
    public SelectTemplate getSelectTemplate() {
        return new SelectTemplate(this, AbstractTemplate.noTemplate());
    }

    public Map<Object, QueryPartType<?>> getTemplates() {
        return templates;
    }

    public QlCode getMainTable() {
        return mainTable;
    }
}
