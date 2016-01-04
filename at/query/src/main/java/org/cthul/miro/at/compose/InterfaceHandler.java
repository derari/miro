package org.cthul.miro.at.compose;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.cthul.miro.composer.Composer;
import org.cthul.miro.composer.ConfigureKey.Configurable;
import org.cthul.miro.composer.InternalComposer;
import org.cthul.miro.db.stmt.MiStatement;

/**
 *
 */
public class InterfaceHandler implements InvocationHandler {
    
    public static <I> I create(Class<I> clazz, Composer composer, MiStatement<?> stmt) {
        InterfaceHandler handler = new InterfaceHandler(composer, stmt);
        Class<?>[] interfaces = {clazz, MiStatement.class, InternalComposer.class};
        Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, handler);
        return (I) proxy;
    }
    
    private static final Set<String> miStatementMethods = new HashSet<>(Arrays.asList(
                            "execute", "_execute", "asAction", "submit"));
    
    private final Composer composer;
    private final MiStatement<?> stmt;

    public InterfaceHandler(Composer composer, MiStatement<?> stmt) {
        this.composer = composer;
        this.stmt = stmt;
    }

    private boolean isMiStatementMethod(Method method) {
        return method.getParameterCount() == 0 && miStatementMethods.contains(method.getName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            final Object result;
            if (method.isDefault()) {
                result = MethodHandles.lookup()
                            .in(method.getDeclaringClass())
                            .unreflectSpecial(method,method.getDeclaringClass())
                            .bindTo(proxy)
                            .invokeWithArguments(args);
            } else if (isMiStatementMethod(method)) {
                return method.invoke(stmt, args);
            } else {
                Configurable impl = composer.node(new MethodKey(method));
                if (impl == null) {
                    if (args != null && args.length > 0) {
                        throw new IllegalArgumentException(method.toString());
                    }
                } else {
                    impl.set(args);
                }
                result = null;
            }
            return result != null ? result : proxy;
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
