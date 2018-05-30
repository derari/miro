package org.cthul.miro.sql.composer.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.sql.SqlJoinableClause;
import org.cthul.miro.sql.SqlTableClause;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.sql.syntax.MiSqlParser;

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
    
    public void requireDependencies(Consumer<Object> target) {
        getDependencies().forEach(target::accept);
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
        
        public From(MiSqlParser.Table t) {
            this(t.getKey(), t);
            t.getColumnRefs().forEach(cf -> {
                String parent = cf.getParentKey();
                if (parent != null) getDependencies().add(parent);
            });
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
        
        public Join(MiSqlParser.JoinPart jp) {
            this(jp.getTable().getKey(), jp.getType(), jp.getTable(), jp.getCondition());
            jp.getTable().getColumnRefs().forEach(cf -> {
                String parent = cf.getParentKey();
                if (parent != null) getDependencies().add(parent);
            });
            jp.getCondition().getColumnRefs().forEach(cf -> {
                String parent = cf.getParentKey();
                if (parent != null && !parent.equals(getKey())) getDependencies().add(parent);
            });
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
