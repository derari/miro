package org.cthul.miro.at.compose;

import org.cthul.miro.at.More;
import org.cthul.miro.at.Where;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.cthul.miro.composer.ComposerParts;
import org.cthul.miro.composer.ConfigureKey;
import org.cthul.miro.composer.Template;
import org.cthul.miro.composer.sql.SqlTemplateKey;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.db.sql.syntax.MiSqlParser;
import org.cthul.miro.map.MappedStatementBuilder;
import org.cthul.objects.instance.Arg;

/**
 *
 * @param <E>
 * @param <S>
 */
public class AnnotationReader<E, S extends SqlFilterableClause> {
    
    private Class<?> iface;
    private final Set<Class<?>> interfaces = new HashSet<>();
    private final BiConsumer<Object, Template<? super MappedStatementBuilder<E, ? extends S>>> addTemplate;
    
    public AnnotationReader(BiConsumer<Object, Template<? super MappedStatementBuilder<E, ? extends S>>> addTemplate) {
        this.addTemplate = addTemplate;
    }

    private void guessPrimaryInterface(Class<?> clazz) {
        if (iface == null || iface.isAssignableFrom(clazz)) {
            iface = clazz;
        }
    }
    
    public Class<?> getPrimaryInterface() {
        return iface;
    }
    
    public void add(Object key, Template<? super MappedStatementBuilder<E, ? extends S>> template) {
        addTemplate.accept(key, template);
    }
    
    public ConfigureKey addNewKey(String keyName, Template<? super MappedStatementBuilder<E, ? extends S>> template) {
        ConfigureKey key = ConfigureKey.unique(keyName);
        add(key, template);
        return key;
    }
    
    public void add(List<ConfigureKey> keys, ConfigureKey key, Template<? super MappedStatementBuilder<E, ? extends S>> template) {
        add(key, template);
        keys.add(key);
    }
    
    public ConfigureKey addNewKey(List<ConfigureKey> keys, String keyName, Template<? super MappedStatementBuilder<E, ? extends S>> template) {
        ConfigureKey key = addNewKey(keyName, template);
        keys.add(key);
        return key;
    }
    
    protected String name(String prefix, String name) {
        if (prefix == null || prefix.isEmpty()) return name;
        return prefix + "-" + name;
    }
    
    public void readInterfaceClass(Class<?> clazz) {
        if (interfaces.add(clazz)) {
            guessPrimaryInterface(clazz);
            for (Class<?> sup: clazz.getInterfaces()) {
                readInterfaceClass(sup);
            }
            readMore(clazz.getSimpleName(), getMore(clazz));
        }
    }
    
    public ConfigureKey readMethod(Method method) {
        readInterfaceClass(method.getDeclaringClass());
        return readMore(method.getName(), getMore(method));
    }
    
    protected ConfigureKey readMore(String prefix, More more) {
        List<ConfigureKey> keys = new ArrayList<>();
        readWhere(prefix, keys, more.where());
        return joinKeys(prefix, keys);
    }
    
    protected void readWhere(String prefix, List<ConfigureKey> keys, Where[] atWheres) {
        ArgumentMappingGroup mapGroup = new ArgumentMappingGroup();
        for (Where atWhere: atWheres) {
            ArgumentMap map = mapGroup.map(atWhere.args(), atWhere.mapArgs());
            for (String condition: atWhere.value()) {
                MiSqlParser.QuickFilter qf = MiSqlParser.tryParseQuickFilter(condition);
                if (qf != null) {
                    keys.add(SqlTemplateKey
                            .unique(name(prefix, qf.getKey()))
                            .withArgs(map.next(1))
                            .quickFilter(qf.getKey(), qf.getOperation()));
                } else {
                    keys.add(SqlTemplateKey
                            .unique(name(prefix, "where"))
                            .withArgs(map.remaining())
                            .where(condition));
                }
            }
        }
    }
    
    protected ConfigureKey joinKeys(String prefix, List<ConfigureKey> keys) {
        keys.removeIf(k -> k == null);
        if (keys.isEmpty()) return null;
        if (keys.size() == 1) return keys.get(0);
        return addNewKey(name(prefix, "[" + keys.size() + "]"),
                ComposerParts.newNode(ic -> new ConfigureKey.Configurable() {
            @Override
            public void set(Object... values) {
                keys.forEach(k -> ic.node(k).set(values));
            }
        }));
    }
    
    private static More getMore(AnnotatedElement element) {
        More m1 = element.getAnnotation(More.class);
        if (m1 == null) m1 = NULL_MORE;
        More m2 = new More() {
            @Override
            public Where[] where() { return atArray(element, Where.class, NO_WHERE); }
            @Override
            public Class<? extends Annotation> annotationType() { return More.class; }
        };
        return joinMore(m1, m2);
    }
    
    private static <T extends Annotation> T[] atArray(AnnotatedElement element, Class<T> atClass, T[] empty) {
        T at = element.getAnnotation(atClass);
        if (at == null) return empty;
        T[] array = (T[]) Array.newInstance(atClass, 1);
        array[0] = at;
        return array;
    }
    
    private static More joinMore(More m1, More m2) {
        if (m1 == NULL_MORE) return m2;
        if (m2 == NULL_MORE) return m1;
        return new More() {
            @Override
            public Where[] where() { return joinArray(m1.where(), m2.where()); }
            @Override
            public Class<? extends Annotation> annotationType() { return More.class; }
        };
    }
    
    private static <T> T[] joinArray(T[] a1, T[] a2) {
        if (a1 == null || a1.length == 0) return a2;
        if (a2 == null || a2.length == 0) return a1;
        T[] r = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, r, a1.length, a2.length);
        return r;
    }
    
    private static final Where[] NO_WHERE = {};
    
    private static final More NULL_MORE = new More() {
        @Override
        public Where[] where() { return NO_WHERE; }
        @Override
        public Class<? extends Annotation> annotationType() { return More.class; }
    };
    
    class ArgumentMappingGroup {
     
        int counter = 0;
        
        public ArgumentMappingGroup() {
        }

        public ArgumentMap map(Arg[] args, int[] argMap) {
            return new ArgumentMap(this);
        }
    }
    
    class ArgumentMap {
        
        final ArgumentMappingGroup group;

        public ArgumentMap(ArgumentMappingGroup group) {
            this.group = group;
        }
        
        public Function<Object[], Object[]> next(int c) {
            int start = group.counter;
            int end = start + c;
            group.counter += c;
            return o -> {
                if (start == 0 && end == o.length) return o;
                return Arrays.copyOfRange(o, start, end);
            };
        }
        
        public Function<Object[], Object[]> remaining() {
            int start = group.counter;
            if (start == 0) return o -> o;
            return o -> Arrays.copyOfRange(o, start, o.length);
        }
    }
}
