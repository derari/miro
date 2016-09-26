package org.cthul.miro.sql.template;

import java.util.HashSet;
import java.util.Set;
import org.cthul.miro.sql.SqlJoinableClause;
import org.cthul.miro.sql.SqlTableClause;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.request.Composer;

/**
 *
 */
public abstract class SqlTable {
    
    private final String key;
    private final Set<Object> dependencies = new HashSet<>();

    public SqlTable(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Set<Object> getDependencies() {
        return dependencies;
    }
    
    public void requireDependencies(Composer c) {
        c.requireAll(getDependencies());
    }
    
    public abstract void writeDefinition(SqlTableClause sb);
    
    public SqlSnippet<SqlTableClause> getSnippet() {
        return SqlSnippet.create(key, dependencies, this::writeDefinition);
    }
    
    public static class From extends SqlTable {
        
        private final QlCode definition;

        public From(String key, QlCode definition) {
            super(key);
            this.definition = definition;
        }

        @Override
        public void writeDefinition(SqlTableClause sb) {
            sb.table().ql(definition);
        }
        
        public SqlSnippet<SqlTableClause> getJoinSnippet(SqlJoinableClause.JoinType jt, QlCode onCondition) {
            return SqlSnippet.create(getKey(), getDependencies(), sql -> {
                SqlJoinableClause.Join<?> join = sql.table().join(jt);
                join.ql(definition).on().ql(onCondition);
            });
        }
    }
    
    public static class Join extends SqlTable {
        
        private final SqlJoinableClause.JoinType jt;
        private final QlCode joinExpression;
        private final QlCode onCondition;

        public Join(String key, SqlJoinableClause.JoinType jt, QlCode joinExpression, QlCode onCondition) {
            super(key);
            this.jt = jt;
            this.joinExpression = joinExpression;
            this.onCondition = onCondition;
        }

        @Override
        public void writeDefinition(SqlTableClause sb) {
            SqlJoinableClause.Join<?> join = sb.table().join(jt);
            join.ql(joinExpression);
            if (onCondition != null) {
                join.on().ql(onCondition);
            }
        }
    }
}