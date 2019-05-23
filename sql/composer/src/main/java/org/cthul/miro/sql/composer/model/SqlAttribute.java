package org.cthul.miro.sql.composer.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.cthul.miro.db.request.StatementBuilder;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public class SqlAttribute {
    
    private final String key, alias;
    private final Set<Object> dependencies = new HashSet<>();
    private final QlCode expression;
    private final QlCode aliasCode;

    public SqlAttribute(MiSqlParser.Attribute at) {
        this(at.getKey(), at.getExpression(), at.getAlias());
        at.getColumnRefs().forEach(cf -> {
            String parent = cf.getParentKey();
            if (parent != null) dependencies.add(parent);
        });
    }

    public SqlAttribute(String key, QlCode expression, QlCode aliasCode) {
        this.key = key;
        this.expression = expression;
        this.aliasCode = aliasCode;
        this.alias = aliasCode != null ? new AliasBuilder().append(aliasCode).toString() : key;
    }

    public String getKey() {
        return key;
    }

    @Deprecated // remove?
    public String getAlias() {
        return alias;
    }

    public Set<Object> getDependencies() {
        return dependencies;
    }
    
    public void writeSelectClause(QlBuilder<?> ql) {
        ql.ql(expression());
        if (aliasCode != null) {
            ql.ql(" AS ").ql(aliasCode);
        }
    }
    
    public void writeSelectClause(QlCode aliasPrefix, QlBuilder<?> ql) {
        ql.ql(expression()).ql(" AS ");
        AliasBuilder prefixedAlias = new AliasBuilder();
        prefixedAlias.append(aliasPrefix);
        if (alias != null) {
            prefixedAlias.append(alias);
        } else {
            prefixedAlias.append(key);
        }
        ql.id(prefixedAlias.toString());
    }
    
    public QlCode expression() {
        return expression;
    }

    public SqlSnippet<SelectBuilder> getSelectSnippet() {
        return SqlSnippet.create(key, dependencies, sql -> {
            writeSelectClause(sql.select());
        });
    }
    
    public SqlSnippet<SelectBuilder> getSelectSnippet(QlCode aliasPrefix) {
        return SqlSnippet.create(key, dependencies, sql -> {
            writeSelectClause(aliasPrefix, sql.select());
        });
    }
    
    // prefixed attribute has no dependencies!
    public SqlAttribute getWithPredix(String aliasPrefix) {
        AliasBuilder prefixedAlias = new AliasBuilder();
        prefixedAlias.append(aliasPrefix);
        if (aliasCode != null) {
            prefixedAlias.append(aliasCode);
        } else {
            prefixedAlias.append(key);
        }
        String key2 = aliasPrefix + key;
        return new SqlAttribute(key2, expression, QlCode.id(prefixedAlias.toString()));
    }
    
    private static class AliasBuilder implements QlBuilder<AliasBuilder> {
        private final StringBuilder sb = new StringBuilder();
        @Override
        public AliasBuilder append(CharSequence query) {
            sb.append(query);
            return this;
        }
        @Override
        public AliasBuilder identifier(String id) {
            return append(id);
        }
        @Override
        public AliasBuilder stringLiteral(String string) {
            throw new UnsupportedOperationException();
        }
        @Override
        public AliasBuilder constant(Object key) {
            throw new UnsupportedOperationException();
        }
        @Override
        public AliasBuilder pushArgument(Object arg) {
            throw new UnsupportedOperationException();
        }
        @Override
        public <Clause> Clause begin(ClauseType<Clause> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <Clause> Clause as(Function<StatementBuilder, Clause> factory) {
            throw new UnsupportedOperationException();
        }
        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
