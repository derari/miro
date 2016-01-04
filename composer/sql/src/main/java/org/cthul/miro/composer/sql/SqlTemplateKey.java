package org.cthul.miro.composer.sql;

import java.util.function.Function;
import java.util.function.Supplier;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.ConfigureKey;
import org.cthul.miro.composer.StatementPart;
import org.cthul.miro.composer.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.db.syntax.QlCode;

/**
 *
 */
public abstract class SqlTemplateKey extends ConfigureKey {

    public SqlTemplateKey(Object value) {
        super(value);
    }
    
    protected abstract Template<? super SqlFilterableClause> asTemplate(SqlTemplates owner);
    
    public static Builder key(Object keyValue) {
        return new Builder(keyValue);
    }
    
    public static Builder unique(Object keyValue) {
        return new Builder(uniqueName(keyValue));
    }
    
    public static class Builder extends SqlTemplateKey {
        
        private Function<Object[], Object[]> argsMapping = NO_ARGS_MAPPING;

        public Builder(Object value) {
            super(value);
        }
        
        public Builder withArgs(Function<Object[], Object[]> argsMapping) {
            this.argsMapping = argsMapping == null ? NO_ARGS_MAPPING : argsMapping;
            return this;
        }
        
        @Override
        protected Template<? super SqlFilterableClause> asTemplate(SqlTemplates owner) {
            throw new UnsupportedOperationException("This is only a builder!");
        }
        
        protected SqlTemplateKey newPartFactory(Function<SqlTemplates, ? extends StatementPart<? super SqlFilterableClause>> factory) {
            return new GeneralKey(getValue(), owner -> {
                return ComposerParts.newNodePart(() -> factory.apply(owner));
            });
        }
        
        protected SqlTemplateKey newPartFactory(Supplier<? extends StatementPart<? super SqlFilterableClause>> factory) {
            return new GeneralKey(getValue(), owner -> {
                return ComposerParts.newNodePart(factory);
            });
        }
        
        public SqlTemplateKey quickFilter(String name, String operation) {
            return newPartFactory(owner -> {
                return new QuickFilterPart(owner, name, operation, argsMapping);
            });
        }
        
        public SqlTemplateKey where(String condition) {
            QlCode qlCondition = MiSqlParser.parseExpression(condition);
            return newPartFactory(() -> {
                return new WherePart(qlCondition, argsMapping);
            });
        }
    }
    
    static class GeneralKey extends SqlTemplateKey {
        
        private final Function<SqlTemplates, Template<? super SqlFilterableClause>> factory;

        public GeneralKey(Object value, Function<SqlTemplates, Template<? super SqlFilterableClause>> factory) {
            super(value);
            this.factory = factory;
        }

        @Override
        protected Template<? super SqlFilterableClause> asTemplate(SqlTemplates owner) {
            return factory.apply(owner);
        }
    }
    
    static abstract class PartBase implements Configurable {
        
        private final Function<Object[], Object[]> argsMapping;
        protected Object[] args = null;

        public PartBase(Function<Object[], Object[]> argsMapping) {
            this.argsMapping = argsMapping;
        }

        @Override
        public void set(Object... values) {
            setMapped(argsMapping.apply(values));
        }

        protected Object[] getArgs() {
            return args;
        }
        
        protected void setMapped(Object[] values) {
            args = values;
        }
    }
    
    static class QuickFilterPart extends PartBase implements StatementPart<SqlFilterableClause> {
        
        private final SqlTemplates owner;
        private final String name;
        private final String operation;

        public QuickFilterPart(SqlTemplates owner, String name, String operation, Function<Object[], Object[]> argsMapping) {
            super(argsMapping);
            this.owner = owner;
            this.name = name;
            this.operation = operation;
        }

        @Override
        public void addTo(SqlFilterableClause builder) {
            Attribute at = owner.getAttributes().get(name);
            SqlFilterableClause.Where<?> w = builder.where();
            at.writeSelector(w);
            w.ql(" ").ql(operation.trim()).ql(" ?");
            w.pushArguments(getArgs());
        }
    }
    
    static class WherePart extends PartBase implements StatementPart<SqlFilterableClause> {
        
        private final QlCode condition;

        public WherePart(QlCode condition, Function<Object[], Object[]> argsMapping) {
            super(argsMapping);
            this.condition = condition;
        }

        @Override
        public void addTo(SqlFilterableClause builder) {
            SqlFilterableClause.Where<?> w = builder.where();
            w.ql(condition);
            w.pushArguments(getArgs());
        }
    }
    
    private static final Function<Object[], Object[]> NO_ARGS_MAPPING = arg -> arg;
}
