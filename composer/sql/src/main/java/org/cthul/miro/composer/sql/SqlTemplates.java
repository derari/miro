package org.cthul.miro.composer.sql;

import org.cthul.miro.composer.StatementHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.ResultScope;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Key;
import org.cthul.miro.view.composer.CRUDTemplateLayer;
import org.cthul.miro.view.composer.SimpleCRUDTemplateLayer;

/**
 *
 */
public class SqlTemplates 
                implements SimpleCRUDTemplateLayer<SqlFilterableClause, SelectQueryBuilder, SqlFilterableClause, SqlFilterableClause>,
                            SqlTemplateLayer<SqlTemplates> {

    private final Map<String, Attribute> attributes = new HashMap<>();
    private final List<Attribute> keyAttributes = new ArrayList<>();
    private final List<Attribute> defaultAttributes = new ArrayList<>();
    private final List<Attribute> optionalAttributes = new ArrayList<>();
    private final Map<Object, Template<?>> templates = new HashMap<>();
    private QlCode mainTable = null;

    public SqlTemplates() {
    }
    
    @Override
    public Template<? super SqlFilterableClause> insertTemplate(Template<? super SqlFilterableClause> template) {
        return template;
    }

    @Override
    public Template<? super SelectQueryBuilder> selectTemplate(Template<? super SelectQueryBuilder> template) {
        return new SelectTemplate(this, new SqlTemplate<>(this, template));
    }

    @Override
    public Template<? super SqlFilterableClause> updateTemplate(Template<? super SqlFilterableClause> template) {
        return template;
    }

    @Override
    public Template<? super SqlFilterableClause> deleteTemplate(Template<? super SqlFilterableClause> template) {
        return template;
    }

    public QlCode getMainTable() {
        return mainTable;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public List<Attribute> getKeyAttributes() {
        return keyAttributes;
    }

    public List<Attribute> getDefaultAttributes() {
        return defaultAttributes;
    }

    public List<Attribute> getOptionalAttributes() {
        return optionalAttributes;
    }

    public Map<Object, Template<?>> getTemplates() {
        return templates;
    }

    @Override
    public SqlTemplates mainTable(QlCode code, String key) {
        mainTable = code;
        templates.put(key, ComposerParts.require(SqlQueryKey.TABLE));
        return this;
    }

    @Override
    public SqlTemplates attribute(ResultScope scope, boolean key, String id, MiSqlParser.Attribute sqlAt) {
        if (scope == null) scope = ResultScope.DEFAULT;
        Attribute at = Attribute.forColumn(id, scope.isInternal(), sqlAt.getTable(), sqlAt.getColumn(), sqlAt.getName());
        attributes.put(id, at);
        templates.put(id, ComposerParts.setUp(SqlQueryKey.ATTRIBUTE, part -> part.add(at)));
        if (key) {
            keyAttributes.add(at);
        }
        switch (scope) {
            case DEFAULT:
                defaultAttributes.add(at);
                break;
            case OPTIONAL:
                optionalAttributes.add(at);
                break;
        }
        return this;
    }

    @Override
    public <V extends Template<? super SqlFilterableClause>> SqlTemplates where(Key<? super V> key, V filter) {
        templates.put(key, filter);
        return this;
    }
    
    private static final Function<StatementHolder<Object>,Object> GET_STATEMENT = StatementHolder::getStatement;
    
    private static <S> Function<StatementHolder<? extends S>,S> getStatementAdapter() {
        return (Function) GET_STATEMENT;
    }
    
    static final CRUDTemplateLayer<
            StatementHolder<? extends SqlFilterableClause>,
            StatementHolder<? extends SelectQueryBuilder>,
            StatementHolder<? extends SqlFilterableClause>,
            StatementHolder<? extends SqlFilterableClause>,
            SqlFilterableClause,
            SelectQueryBuilder,
            SqlFilterableClause,
            SqlFilterableClause> TEMPLATE_ADAPTER = CRUDTemplateLayer.adapter(getStatementAdapter(), getStatementAdapter(), getStatementAdapter(), getStatementAdapter());
}
