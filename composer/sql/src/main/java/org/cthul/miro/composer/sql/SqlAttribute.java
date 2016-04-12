package org.cthul.miro.composer.sql;

import java.util.HashSet;
import java.util.Set;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.ClauseType;
import org.cthul.miro.db.syntax.QlBuilder;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public class SqlAttribute {
    
    private final String key;
    private final Set<Object> dependencies = new HashSet<>();
    private final QlCode expression;
    private final QlCode alias;

    public SqlAttribute(MiSqlParser.Attribute at) {
        this(at.getKey(), at.getExpression(), at.getAlias());
        at.getColumnRefs().forEach(cf -> {
            String parent = cf.getParentKey();
            if (parent != null) dependencies.add(parent);
        });
    }

    public SqlAttribute(String key, QlCode expression, QlCode alias) {
        this.key = key;
        this.expression = expression;
        this.alias = alias;
    }

    public String getKey() {
        return key;
    }

    public Set<Object> getDependencies() {
        return dependencies;
    }
    
    public void writeSelectClause(QlBuilder<?> ql) {
        ql.ql(expression());
        if (alias != null) {
            ql.ql(" AS ").ql(alias);
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
        public AliasBuilder pushArgument(Object arg) {
            throw new UnsupportedOperationException();
        }
        @Override
        public <Clause> Clause begin(ClauseType<Clause> type) {
            throw new UnsupportedOperationException();
        }
        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
