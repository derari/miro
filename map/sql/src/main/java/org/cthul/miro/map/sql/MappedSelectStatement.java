package org.cthul.miro.map.sql;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.ConfigureKey;
import org.cthul.miro.composer.ConfigureKey.Configurable;
import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.impl.AbstractTemplate;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.db.sql.SelectQuery;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.SqlDQML;
import org.cthul.miro.db.sql.impl.SimpleSelectQuery;
import org.cthul.miro.entity.EntityType;
import org.cthul.miro.graph.Graph;
import org.cthul.miro.graph.GraphSchema;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.miro.map.impl.MappedQueryComposer;

/**
 *
 */
public class MappedSelectStatement<Entity> extends MappedQueryComposer<Entity, SelectQuery>  {

    private SimpleSelectQuery sql;
    private final Set<Object> onceGuard = new HashSet<>();
    private final Map<ConfigureKey, Configurable> actions = new HashMap<>();

    public MappedSelectStatement(EntityType<Entity> entityType, Template<? super MappedStatementBuilder<Entity, SelectQuery>> template) {
        this(entityType, new InternalTemplate<>(template));
    }

    public MappedSelectStatement(Class<Entity> clazz, Graph graph, Template<? super MappedStatementBuilder<Entity, SelectQuery>> template) {
        this(clazz, graph, new InternalTemplate<>(template));
    }

    public MappedSelectStatement(Class<Entity> clazz, GraphSchema schema, Template<? super MappedStatementBuilder<Entity, SelectQuery>> template) {
        this(clazz, schema, new InternalTemplate<>(template));
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    private MappedSelectStatement(EntityType<Entity> entityType, InternalTemplate<Entity> template) {
        super(entityType, SqlDQML.SELECT, template);
        template.owner = this;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private MappedSelectStatement(Class<Entity> clazz, Graph graph, InternalTemplate<Entity> template) {
        super(clazz, graph, SqlDQML.SELECT, template);
        template.owner = this;
    }

    @SuppressWarnings("LeakingThisInConstructor")
    private MappedSelectStatement(Class<Entity> clazz, GraphSchema schema, InternalTemplate<Entity> template) {
        super(clazz, schema, SqlDQML.SELECT, template);
        template.owner = this;
    }
    
    @Override
    protected void initialize() {
        super.initialize();
    }
    
    protected SelectQueryBuilder sql() {
        return sql;
    }
    
    protected void setUp(String key, Configurable cfg) {
        ConfigureKey cfgKey = ConfigureKey.key(key);
        actions.put(cfgKey, cfg);
    }
    
    protected void snippet(String key, Runnable r) {
        setUp(key, args -> {
            if (args != null && args.length > 0) {
                throw new IllegalArgumentException(key + 
                        ": No arguments expected, got " + Arrays.toString(args));
            }
            once(key, r);
        });
    }
    
    protected void snippet(String key, Consumer<SelectQueryBuilder> c) {
        setUp(key, args -> {
            if (args != null && args.length > 0) {
                throw new IllegalArgumentException(key + 
                        ": No arguments expected, got " + Arrays.toString(args));
            }
            once(key, c);
        });
    }
    
    protected Configurable cfg(String key) {
        return node(ConfigureKey.key(key));
    }
    
    protected void once(Object key, Runnable r) {
        if (onceGuard.add(key)) {
            r.run();
        }
    }
    
    protected void once(Runnable r) {
        once(r.getClass(), r);
    }
    
    protected void once(Object key, Consumer<SelectQueryBuilder> c) {
        if (onceGuard.add(key)) {
            c.accept(sql());
        }
    }
    
    protected void once(Consumer<SelectQueryBuilder> c) {
        once(c.getClass(), c);
    }
    
    @Override
    public void require(Object key) {
        if (key instanceof MiConnection) {
            sql = SimpleSelectQuery.create((MiConnection) key);
        }
        super.require(key);
    }

    @Override
    protected SelectQuery newStatement() {
        SelectQuery qry = super.newStatement();
        sql.appendTo(qry);
        return qry;
    }
    
    private static class InternalTemplate<Entity> extends AbstractTemplate<MappedStatementBuilder<Entity, SelectQuery>> {

        private MappedSelectStatement<Entity> owner = null;
        
        public InternalTemplate(Template<? super MappedStatementBuilder<Entity, SelectQuery>> parent) {
            super(parent);
        }

        @Override
        protected Template<? super MappedStatementBuilder<Entity, SelectQuery>> createPartType(Object key) {
            if (key instanceof String) {
                ConfigureKey cfgKey = ConfigureKey.key(key);
                if (owner.actions.containsKey(cfgKey)) {
                    return ComposerParts.setUp(cfgKey, c -> c.set());
                }
            }
            if (key instanceof ConfigureKey) {
                ConfigureKey cfgKey = (ConfigureKey) key;
                Configurable c = owner.actions.get(cfgKey);
                if (c != null) {
                    return ComposerParts.newNode(() -> c);
                }
            }
            return null;
        }
    }
}
