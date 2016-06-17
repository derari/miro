package org.cthul.miro.sql.template;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.sql.SqlFilterableClause;
import org.cthul.miro.sql.SqlJoinableClause.JoinType;
import org.cthul.miro.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;
import org.cthul.miro.util.Key;
import org.cthul.miro.sql.template.JoinedView;

public interface SqlTemplatesBuilder<This extends SqlTemplatesBuilder<This>> {
    
    default This sql(String sql) {
        MiSqlParser.SelectStmt stmt = MiSqlParser.parsePartialSelect(sql);
        stmt.getSelectParts().forEach(sp -> {
            sp.getAttributes().forEach(at -> {
                attribute(new SqlAttribute(at));
            });
        });
        stmt.getTableParts().forEach(tp -> {
            MiSqlParser.Table t = tp.getTable();
            from(t, t.getKey());
        });
        stmt.getJoinParts().forEach(jp -> {
            join(jp.getTable().getKey(), jp.getType(), jp.getTable(), jp.getTable());
        });
        return (This) this;
    }
    
    default This attributes(String... attributes) {
        This me = (This) this;
        for (String a: attributes) {
            me = attributes(MiSqlParser.parseAttributes(a)
                    .stream().map(at -> new SqlAttribute(at))
                    .collect(Collectors.toList()));
        }
        return me;
    }
    
    default This attributes(List<SqlAttribute> attributes) {
        This me = (This) this;
        for (SqlAttribute at: attributes) {
            me = attribute(at);
        }
        return me;
    }
    
    default This attribute(String attribute) {
        MiSqlParser.Attribute at = MiSqlParser.parseAttribute(attribute);
        return attribute(new SqlAttribute(at));
    }
    
    This attribute(SqlAttribute attribute);
    
    default This from(String table) {
        MiSqlParser.Table t = MiSqlParser.parseFromPart(table).getTable();
        return from(t, t.getKey());
    }
    
    default This from(String schema, String table, String key) {
        QlCode.Fluent code;
        if (schema == null || schema.isEmpty()) { 
            code = QlCode.id(table);
        } else {
            code = QlCode.id(schema, table);
        }
        if (key != null && !key.isEmpty()) {
            code = code.ql(" ").ql(key);
        }
        return from(code, key);
    }
    
    default This from(QlCode code, String key) {
        return table(new SqlTable.From(key, code));
    }
    
    This table(SqlTable table);
    
    default This join(String join) {
        MiSqlParser.JoinPart jp = MiSqlParser.parseJoinPart(join);
        return join(jp.getTable().getKey(), jp.getType(), jp.getTable(), jp.getCondition());
    }
    
    default This join(String key, JoinType jt, QlCode declaration, QlCode onClause) {
        return table(new SqlTable.Join(key, jt, declaration, onClause));
    }
    
    This join(JoinedView view);
    
    <V extends Template<? super SqlFilterableClause>> This where(Key<? super V> key, V filter);
    
    default Using<This> using(Object... dependencies) {
        return using(Arrays.asList(dependencies));
    }
    
    default Using<This> using(List<Object> dependencies) {
        class U implements Using<This> {
            @Override
            public List<Object> dependencies() {
                return dependencies;
            }
            @Override
            public This actualSqlTemplatesBuilder() {
                return (This) SqlTemplatesBuilder.this;
            }
        }
        return new U();
    }
    
    interface Using<B extends SqlTemplatesBuilder<B>> extends SqlTemplatesBuilder<B> {
        
        List<Object> dependencies();
        
        B actualSqlTemplatesBuilder();
        
        default B define(Consumer<? super B> action) {
            class UDelegator implements Delegator<B> {
                @Override
                public SqlTemplatesBuilder<?> internalSqlTemplatesBuilder() {
                    return Using.this;
                }

                @Override
                public B join(JoinedView view) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            }
            action.accept((B) new UDelegator());
            return actualSqlTemplatesBuilder();
        }

        @Override
        default B table(SqlTable table) {
            table.getDependencies().addAll(dependencies());
            return actualSqlTemplatesBuilder().table(table);
        }

        @Override
        public default B attribute(SqlAttribute attribute) {
            attribute.getDependencies().addAll(dependencies());
            return actualSqlTemplatesBuilder().attribute(attribute);
        }

        @Override
        public default B join(JoinedView view) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        default <V extends Template<? super SqlFilterableClause>> B where(Key<? super V> key, V filter) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    interface Delegator<This extends SqlTemplatesBuilder<This>> extends SqlTemplatesBuilder<This> {

        SqlTemplatesBuilder<?> internalSqlTemplatesBuilder();

        @Override
        default This attribute(SqlAttribute attribute) {
            internalSqlTemplatesBuilder().attribute(attribute);
            return (This) this;
        }

        @Override
        default This table(SqlTable table) {
            internalSqlTemplatesBuilder().table(table);
            return (This) this;
        }

        @Override
        default This join(JoinedView view) {
            internalSqlTemplatesBuilder().join(view);
            return (This) this;
        }

        @Override
        default<V extends Template<? super SqlFilterableClause>> This where(Key<? super V> key, V filter) {
            internalSqlTemplatesBuilder().where(key, filter);
            return (This) this;
        }
    }
}
