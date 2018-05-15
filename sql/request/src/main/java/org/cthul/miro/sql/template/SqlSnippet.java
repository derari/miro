package org.cthul.miro.sql.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.part.Configurable;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.sql.SelectBuilder;
import org.cthul.miro.sql.syntax.MiSqlParser.SelectStmt;
import org.cthul.miro.util.Key;

/**
 *
 * @param <Builder>
 */
public abstract class SqlSnippet<Builder> implements Template<Builder> {
    
    public static <B> SqlSnippet<B> create(String key, Set<Object> dependencies, BiConsumer<? super B, ? super Object[]> writePart) {
        return new SqlSnippet<B>(key, dependencies) {
            @Override
            protected void writePart(B builder, Object[] args) {
                writePart.accept(builder, args);
            }
        };
    }
    
    public static SqlSnippet<SelectBuilder> select(String key, SelectStmt stmt) {
        Set<String> dependencies = new LinkedHashSet<>();
        stmt.getSelectParts().stream()
                .flatMap(sp -> sp.getAttributes().stream())
                .flatMap(at -> at.getColumnRefs().stream())
                .forEach(cr -> dependencies.add(cr.getParentKey()));
        stmt.getTableParts().stream()
                .map(tp -> tp.getTable())
                .flatMap(t -> t.getColumnRefs().stream())
                .forEach(cr -> dependencies.add(cr.getParentKey()));
        stmt.getJoinParts().stream()
                .flatMap(jp -> Arrays.asList(jp.getTable(), jp.getCondition()).stream())
                .flatMap(t -> t.getColumnRefs().stream())
                .forEach(cr -> dependencies.add(cr.getParentKey()));
        Arrays.asList(stmt.getWhereParts(), stmt.getGroupByParts(),
                stmt.getHavingParts(), stmt.getOrderByParts()).stream()
                .flatMap(list -> list.stream())
                .flatMap(ep -> ep.getExpression().getColumnRefs().stream())
                .forEach(cr -> dependencies.add(cr.getParentKey()));        
        return new SqlSnippet<SelectBuilder>(key) {
            @Override
            protected void writePart(SelectBuilder builder, Object[] args) {
                if (args != null && args.length > 0) {
                    throw new IllegalArgumentException(
                            key + ": no arguments expected, got " + 
                            Arrays.toString(args));
                }
                stmt.appendTo(builder);
            }
        };
    }
    
    public static <B> SqlSnippet<B> create(String key, Set<Object> dependencies, Consumer<? super B> writePart) {
        return new SqlSnippet<B>(key, dependencies) {
            @Override
            protected void writePart(B builder, Object[] args) {
                if (args != null && args.length > 0) {
                    throw new IllegalArgumentException(
                            key + ": no arguments expected, got " + 
                            Arrays.toString(args));
                }
                writePart.accept(builder);
            }
        };
    }
    
    private final String key;
    private final Set<Object> dependencies;

    public SqlSnippet(String key) {
        this.key = key;
        dependencies = new HashSet<>();
    }

    public SqlSnippet(String key, Set<Object> dependencies) {
        this.key = key;
        this.dependencies = dependencies;
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

    @Override
    public void addTo(Object key, InternalComposer<? extends Builder> composer) {
        composer.addPart((Key) key, new Part());
    }
    
    public void addAsNode(Object key, InternalComposer<? extends Builder> composer) {
        composer.addNode((Key) key, new Part());
    }
    
    public void addNodeAsPart(Object key, InternalComposer<? extends Builder> composer) {
        composer.addPart((Part) composer.node((Key) key));
    }
    
    public Part newPart() {
        return new Part();
    }
    
    protected abstract void writePart(Builder builder, Object[] args);

    @Override
    public String toString() {
        return key;
    }
    
    public class Part implements StatementPart<Builder>, Copyable<Builder>, Configurable {
        
        private Object[] args;

        public Part() {
        }

        public Part(Object[] args) {
            this.args = args;
        }

        @Override
        public void addTo(Builder builder) {
            writePart(builder, args);
        }

        @Override
        public void set(Object... values) {
            this.args = values;
        }

        @Override
        public Object copyFor(CopyComposer<Builder> cc) {
            return new Part(args);
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }

        @Override
        public String toString() {
            return SqlSnippet.this.toString() + 
                    (args == null || args.length == 0 ? "" : Arrays.toString(args));
        }
    }
}
