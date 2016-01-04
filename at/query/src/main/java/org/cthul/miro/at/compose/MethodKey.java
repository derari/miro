package org.cthul.miro.at.compose;

import java.lang.reflect.Method;
import java.util.Objects;
import org.cthul.miro.composer.ConfigureKey;
import org.cthul.miro.util.Key;

/**
 *
 */
public class MethodKey extends ConfigureKey {
    
    private final Method method;

    public MethodKey(Method method) {
        super(signature(method));
        this.method = method;
    }
    
    private static String signature(Method m) {
        StringBuilder sb = new StringBuilder(m.getName());
        sb.append('(');
        for (Class<?> c: m.getParameterTypes()) {
            sb.append(c.getCanonicalName()).append(';');
        }
        return sb.append(')').toString();
    }

    public Method getMethod() {
        return method;
    }
}
