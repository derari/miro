package org.cthul.miro.migrate.at;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cthul.miro.db.MiException;
import org.cthul.miro.migrate.Migration;
import org.cthul.miro.migrate.Version;
import org.cthul.objects.instance.InstanceMap;

/**
 *
 */
public class MigrationMethodScanner<DB> {
    
    private final InstanceMap cfg;

    public MigrationMethodScanner(InstanceMap cfg) {
        this.cfg = cfg;
    }
    
    public List<Migration<DB>> read(Class<?> clazz) {
        return read(clazz, null);
    }
    
    public List<Migration<DB>> read(Object instance) {
        return read(instance.getClass(), instance);
    }
    
    public List<Migration<DB>> read(Class<?> clazz, Object instance) {
        if (instance != null) {
            cfg.getOrCreate((Class) clazz, m -> instance);
        }
        Migrations<DB> mig = new Migrations<>();
        for (Method m: clazz.getMethods()) {
            if (m.getParameterCount() != 1) continue;
            Up atUp = m.getAnnotation(Up.class);
            if (atUp != null || m.getName().startsWith("up_")) {
                String v = version(atUp, m.getName());
                mig.addUp(Version.parse(v), action(clazz, instance, m));
            }
        }
        return mig.asList();
    }
    
    private String version(Up atUp, String name) {
        String v = atUp == null ? "" : atUp.value();
        if (!v.isEmpty()) return v;
        return versionFromName(name);
    }
    
    private String versionFromName(String name) {
        int u = name.indexOf('_');
        if (u < 0) return name;
        return name.substring(u);
    }
    
    private Action<DB> action(Class<?> clazz, Object instance, Method m) {
        return db -> {
            Object inst = instance;
            if (inst == null && (m.getModifiers() & Modifier.STATIC) == 0) {
                inst = cfg.getOrCreate(clazz);
            }
            try {
                m.invoke(inst, db);
            } catch (InvocationTargetException e) {
                throw new MiException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new MiException(e);
            }
        };
    }
    
    private static class Migrations<DB> {
        private final Set<Version> versions = new HashSet<>();
        private final Map<Version, Action<DB>> ups = new HashMap<>();
        private final Map<Version, Action<DB>> downs = new HashMap<>();

        public Migrations() {
        }
        
        public void addUp(Version v, Action<DB> up) {
            ups.put(v, up);
            versions.add(v);
        }
        
        public void addDown(Version v, Action<DB> down) {
            downs.put(v, down);
            versions.add(v);
        }
        
        public List<Migration<DB>> asList() {
            return versions.stream()
                    .map(v -> new Migration<DB>() {
                @Override
                public Version getVersion() {
                    return v;
                }
                @Override
                public void up(DB db) throws MiException {
                    Action<DB> up = ups.get(v);
                    if (up == null) throw new UnsupportedOperationException("Up: " + v);
                    up.run(db);
                }
                @Override
                public void down(DB db) throws MiException {
                    Action<DB> down = downs.get(v);
                    if (down == null) throw new UnsupportedOperationException("Down: " + v);
                    down.run(db);
                }
            }).collect(Collectors.toList());
        }
    }
    
    private static interface Action<DB> {
        
        void run(DB db) throws MiException;
    }
}
